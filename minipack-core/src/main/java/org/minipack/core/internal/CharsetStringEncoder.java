/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import org.minipack.core.*;

/// A string encoder backed by [CharsetEncoder].
public final class CharsetStringEncoder implements MessageEncoder<CharSequence> {
  private static final byte[] REPLACEMENT_CHARACTER =
      new byte[] {(byte) 0xef, (byte) 0xbf, (byte) 0xbd}; // `\ufffd` in UTF-8

  // CharsetEncoder is up to 10x faster than an optimized hand-written UTF-8 encoder.
  // https://cl4es.github.io/2021/10/17/Faster-Charset-Encoding.html
  //
  // We should not use String.getBytes() or StandardCharsets.UTF_8.encode()
  // because these methods replace malformed/unmappable characters with the non-standard `?`.
  // https://www.reversemode.com/2023/03/beware-of-javas-stringgetbytes.html
  //
  // Potential optimization opportunities:
  // * support UTF-16
  // * use Java Vector API
  // * support TruffleString (for Truffle languages)
  private final CharsetEncoder charsetEncoder =
      StandardCharsets.UTF_8.newEncoder()
          .onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .replaceWith(REPLACEMENT_CHARACTER);

  @Override
  public void encode(CharSequence charSeq, MessageSink sink) throws IOException {
    var charLength = charSeq.length();
    if (charLength == 0) {
      sink.write(MessageFormat.FIXSTR_PREFIX);
      return;
    }
    var sinkBuffer = sink.buffer();
    var allocator = sink.allocator();
    var maxByteLength = Math.multiplyExact(charLength, 3);
    var headerLength =
        maxByteLength < 1 << 5 ? 1 : maxByteLength < 1 << 8 ? 2 : maxByteLength < 1 << 16 ? 3 : 5;
    sink.ensureRemaining(headerLength);
    var headerPosition = sinkBuffer.position();
    sinkBuffer.position(headerPosition + headerLength); // filled in by fillInHeader()
    CharBuffer charBuffer;
    LeasedCharBuffer leasedCharBuffer = null;
    if (sinkBuffer.hasArray() && charSeq instanceof String string) {
      // Copy string to char array because CharsetEncoder.encode() is up to
      // 10x faster if both charBuffer and byteBuffer have accessible array.
      // https://cl4es.github.io/2021/10/17/Faster-Charset-Encoding.html
      leasedCharBuffer = allocator.getCharBuffer(charLength);
      charBuffer = leasedCharBuffer.get().limit(charLength);
      string.getChars(0, charLength, charBuffer.array(), 0);
    } else if (charSeq instanceof CharBuffer buffer) {
      charBuffer = buffer;
    } else {
      charBuffer = CharBuffer.wrap(charSeq);
    }
    var byteBuffer = sinkBuffer; // fill sink buffer before switching to extra buffer
    LeasedByteBuffer leasedByteBuffer = null;
    try {
      charsetEncoder.reset();
      var result = charsetEncoder.encode(charBuffer, byteBuffer, true);
      if (result.isOverflow()) {
        leasedByteBuffer =
            allocator.getByteBuffer(
                headerLength + maxByteLength - byteBuffer.position() + headerPosition);
        byteBuffer = leasedByteBuffer.get();
        result = charsetEncoder.encode(charBuffer, byteBuffer, true);
      }
      assert !result.isError(); // we use CodingErrorAction.REPLACE
      result = charsetEncoder.flush(byteBuffer);
      assert !result.isOverflow(); // should never happen for UTF-8
      fillInHeader(headerPosition, headerLength, sinkBuffer, byteBuffer);
      if (leasedByteBuffer != null) {
        sink.write(byteBuffer.flip());
      }
    } finally {
      if (leasedByteBuffer != null) {
        leasedByteBuffer.close();
      }
      if (leasedCharBuffer != null) {
        leasedCharBuffer.close();
      }
    }
  }

  private static void fillInHeader(
      int headerPosition, int headerLength, ByteBuffer sinkBuffer, ByteBuffer byteBuffer) {
    var byteLength = sinkBuffer.position() - (headerPosition + headerLength);
    if (byteBuffer != sinkBuffer) { // same as `leasedByteBuffer != null`
      byteLength += byteBuffer.position();
    }
    switch (headerLength) {
      case 1 -> sinkBuffer.put(headerPosition, (byte) (MessageFormat.FIXSTR_PREFIX | byteLength));
      case 2 ->
          sinkBuffer
              .put(headerPosition, MessageFormat.STR8)
              .put(headerPosition + 1, (byte) byteLength);
      case 3 ->
          sinkBuffer
              .put(headerPosition, MessageFormat.STR16)
              .putShort(headerPosition + 1, (short) byteLength);
      default ->
          sinkBuffer
              .put(headerPosition, MessageFormat.STR32)
              .putInt(headerPosition + 1, byteLength);
    }
  }
}
