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
  private static final int MAX_FIXSTR_LENGTH = (1 << 5) - 1;
  private static final int MAX_STR8_LENGTH = (1 << 8) - 1;
  private static final int MAX_STR16_LENGTH = (1 << 16) - 1;
  private static final int MAX_HEADER_SIZE = 5;
  private static final int MAX_ENCODER_SUFFIX_SIZE = 8; // JDK 21 encoders flush at most 3 bytes

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
    var maxByteLength = charLength * (long) charsetEncoder.maxBytesPerChar();
    var sinkBuffer = sink.buffer();
    // fill sink buffer before switching to extra buffer
    var byteBuffer =
        sinkBuffer.remaining() >= MAX_HEADER_SIZE
            ? sink.buffer()
            : sink.allocator().byteBuffer(MAX_HEADER_SIZE + maxByteLength);
    var headerBuffer = byteBuffer;
    var headerPosition = headerBuffer.position();
    var headerLength =
        maxByteLength <= MAX_FIXSTR_LENGTH
            ? 1
            : maxByteLength <= MAX_STR8_LENGTH ? 2 : maxByteLength <= MAX_STR16_LENGTH ? 3 : 5;
    byteBuffer.position(headerPosition + headerLength);
    var charBuffer =
        charSeq instanceof String str
            // copy string to char array because CharsetEncoder.encode() is up to
            // 10x faster if both charBuffer and byteBuffer have accessible array
            ? CharBuffer.wrap(str.toCharArray())
            : charSeq instanceof CharBuffer buf ? buf : CharBuffer.wrap(charSeq);
    charsetEncoder.reset();
    var result = charsetEncoder.encode(charBuffer, byteBuffer, true);
    if (result.isOverflow()) {
      byteBuffer =
          sink.allocator().byteBuffer(maxByteLength - byteBuffer.position() + headerPosition);
      result = charsetEncoder.encode(charBuffer, byteBuffer, true);
    }
    if (result.isError()) {
      throw Exceptions.codingError(result, charBuffer.position());
    }
    assert !result.isOverflow();
    result = charsetEncoder.flush(byteBuffer);
    if (result.isOverflow()) {
      byteBuffer = sink.allocator().byteBuffer(MAX_ENCODER_SUFFIX_SIZE);
      result = charsetEncoder.flush(byteBuffer);
      assert !result.isOverflow();
    }
    var byteLength = byteBuffer == sinkBuffer
        ? byteBuffer.position() - headerPosition
        : byteBuffer.position() + sinkBuffer.position() - headerPosition;
    switch (headerLength) {
      case 1 -> headerBuffer.put(headerPosition, (byte) (MessageFormat.FIXSTR_PREFIX | byteLength));
      case 2 ->
          headerBuffer.putShort(headerPosition, (short) (MessageFormat.STR8 << 8 | byteLength));
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
      sink.write(byteBuffer);
      sink.allocator().release(byteBuffer);
    }
  }
}
