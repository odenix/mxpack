/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.minipack.java.*;

public final class CharsetStringEncoder implements MessageEncoder<CharSequence> {
  private static final int MAX_HEADER_SIZE = 5;
  private static final int MAX_ENCODER_SUFFIX_SIZE = 8; // 3 as of JDK 21

  private final CharsetEncoder charsetEncoder;

  private static final class Options implements MessageEncoder.StringOptions {
    private @Nullable CharsetEncoder charsetEncoder;

    @Override
    public Options charsetEncoder(CharsetEncoder encoder) {
      charsetEncoder = encoder;
      return this;
    }
  }

  public CharsetStringEncoder() {
    this(options -> {});
  }

  public CharsetStringEncoder(Consumer<MessageEncoder.StringOptions> consumer) {
    var options = new Options();
    consumer.accept(options);
    charsetEncoder =
        options.charsetEncoder != null
            ? options.charsetEncoder
            : StandardCharsets.UTF_8.newEncoder();
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
    sink.ensureRemaining(headerLength);
    var headerPosition = sinkBuffer.position();
    sinkBuffer.position(headerPosition + headerLength); // filled in by fillInHeader()
    CharBuffer charBuffer;
    BufferAllocator.PooledCharBuffer pooledCharBuffer = null;
    if (sinkBuffer.hasArray() && charSeq instanceof String string) {
      // Copy string to char array because CharsetEncoder.encode() is up to
      // 10x faster if both charBuffer and byteBuffer have accessible array.
      // https://cl4es.github.io/2021/10/17/Faster-Charset-Encoding.html
      pooledCharBuffer = allocator.getCharBuffer(charLength);
      charBuffer = pooledCharBuffer.value().limit(charLength);
      string.getChars(0, charLength, charBuffer.array(), 0);
    } else if (charSeq instanceof CharBuffer buffer) {
      charBuffer = buffer;
    } else {
      charBuffer = CharBuffer.wrap(charSeq);
    }
    var byteBuffer = sinkBuffer; // fill sink buffer before switching to extra buffer
    BufferAllocator.PooledByteBuffer pooledByteBuffer = null;
    try {
      charsetEncoder.reset();
      var result = charsetEncoder.encode(charBuffer, byteBuffer, true);
      if (result.isOverflow()) {
        pooledByteBuffer =
            allocator.getByteBuffer(
                headerLength + maxByteLength - byteBuffer.position() + headerPosition);
        byteBuffer = pooledByteBuffer.value();
        result = charsetEncoder.encode(charBuffer, byteBuffer, true);
      }
      if (result.isError()) {
        throw Exceptions.stringEncodingError(result, charBuffer);
      }
      result = charsetEncoder.flush(byteBuffer);
      if (result.isOverflow()) {
        pooledByteBuffer = allocator.getByteBuffer(MAX_ENCODER_SUFFIX_SIZE);
        byteBuffer = pooledByteBuffer.value();
        charsetEncoder.flush(byteBuffer);
      }
      fillInHeader(headerPosition, headerLength, sinkBuffer, byteBuffer);
      if (pooledByteBuffer != null) {
        sink.write(byteBuffer.flip());
      }
    } finally {
      if (pooledByteBuffer != null) {
        pooledByteBuffer.close();
      }
      if (pooledCharBuffer != null) {
        pooledCharBuffer.close();
      }
    }
  }

  private static void fillInHeader(
      int headerPosition, int headerLength, ByteBuffer sinkBuffer, ByteBuffer byteBuffer) {
    var byteLength = sinkBuffer.position() - (headerPosition + headerLength);
    if (byteBuffer != sinkBuffer) { // same as `pooledByteBuffer != null`
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
