/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import org.minipack.core.*;

public final class CharsetStringEncoder implements MessageEncoder<CharSequence> {
  private static final int MAX_HEADER_SIZE = 5;
  private static final int MAX_ENCODER_SUFFIX_SIZE = 8; // 3 for JDK 21 encoders

  private final CharsetEncoder charsetEncoder;

  public CharsetStringEncoder(CharsetEncoder charsetEncoder) {
    this.charsetEncoder = charsetEncoder;
  }

  @Override
  public void encode(CharSequence charSeq, MessageSink sink, MessageWriter writer)
      throws IOException {
    var charLength = charSeq.length();
    if (charLength == 0) {
      writer.writeStringHeader(0);
      return;
    }
    var sinkBuffer = sink.buffer();
    var allocator = sink.allocator();
    var maxByteLength = charLength * (long) charsetEncoder.maxBytesPerChar();
    var headerLength =
        maxByteLength < 1 << 5 ? 1 : maxByteLength < 1 << 8 ? 2 : maxByteLength < 1 << 16 ? 3 : 5;
    // fill sink buffer before switching to extra buffer
    var byteBuffer =
        sinkBuffer.remaining() >= headerLength
            ? sinkBuffer
            : allocator.byteBuffer(headerLength + maxByteLength);
    CharBuffer allocatedCharBuffer = null;
    try {
      var headerBuffer = byteBuffer;
      var headerPosition = headerBuffer.position();
      byteBuffer.position(headerPosition + headerLength);
      CharBuffer charBuffer;
      if (byteBuffer.hasArray() && charSeq instanceof String string) {
        // Copy string to char array because CharsetEncoder.encode() is up to
        // 10x faster if both charBuffer and byteBuffer have accessible array.
        // https://cl4es.github.io/2021/10/17/Faster-Charset-Encoding.html
        charBuffer = allocator.charBuffer(charLength).limit(charLength);
        allocatedCharBuffer = charBuffer;
        string.getChars(0, charLength, charBuffer.array(), 0);
      } else if (charSeq instanceof CharBuffer buffer) {
        charBuffer = buffer;
      } else {
        charBuffer = CharBuffer.wrap(charSeq);
      }
      charsetEncoder.reset();
      var result = charsetEncoder.encode(charBuffer, byteBuffer, true);
      if (result.isOverflow()) {
        assert byteBuffer == sinkBuffer;
        byteBuffer =
            allocator.byteBuffer(
                headerLength + maxByteLength - byteBuffer.position() + headerPosition);
        result = charsetEncoder.encode(charBuffer, byteBuffer, true);
        assert !result.isOverflow();
      }
      if (result.isError()) {
        throw Exceptions.codingError(result, charBuffer.position());
      }
      result = charsetEncoder.flush(byteBuffer);
      if (result.isOverflow()) {
        assert byteBuffer == sinkBuffer;
        byteBuffer = allocator.byteBuffer(MAX_ENCODER_SUFFIX_SIZE);
        result = charsetEncoder.flush(byteBuffer);
        assert !result.isOverflow();
      }
      var byteLength = headerBuffer.position() - (headerPosition + headerLength);
      if (byteBuffer != headerBuffer) byteLength += byteBuffer.position();
      assert byteLength <= maxByteLength;
      switch (headerLength) {
        case 1 ->
            headerBuffer.put(headerPosition, (byte) (MessageFormat.FIXSTR_PREFIX | byteLength));
        case 2 ->
            headerBuffer
                .put(headerPosition, MessageFormat.STR8)
                .put(headerPosition + 1, (byte) byteLength);
        case 3 ->
            headerBuffer
                .put(headerPosition, MessageFormat.STR16)
                .putShort(headerPosition + 1, (short) byteLength);
        default ->
            headerBuffer
                .put(headerPosition, MessageFormat.STR32)
                .putInt(headerPosition + 1, byteLength);
      }
      if (byteBuffer != sinkBuffer) { // extra buffer was allocated
        sink.write(byteBuffer.flip());
      }
    } finally {
      if (byteBuffer != sinkBuffer) {
        allocator.release(byteBuffer);
      }
      if (allocatedCharBuffer != null) {
        allocator.release(allocatedCharBuffer);
      }
    }
  }
}
