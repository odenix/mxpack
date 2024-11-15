/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.minipack.java.MessageEncoder;
import org.minipack.java.MessageSink;
import org.minipack.java.MessageWriter;

public final class IdentifierEncoder implements MessageEncoder<String> {
  private final Map<String, byte[]> cache = new HashMap<>();
  private final CharsetEncoder charsetEncoder;
  private final int maxCacheSize;
  private int cacheSize = 0;

  private static final class Options implements MessageEncoder.IdentifierOptions {
    private @Nullable CharsetEncoder charsetEncoder;
    private int maxCacheSize = 1024 * 1024;

    @Override
    public IdentifierOptions charsetEncoder(CharsetEncoder encoder) {
      charsetEncoder = encoder;
      return this;
    }

    @Override
    public IdentifierOptions maxCacheSize(int size) {
      maxCacheSize = size;
      return this;
    }
  }

  public IdentifierEncoder(CharsetEncoder charsetEncoder, int maxCacheSize) {
    this.charsetEncoder = charsetEncoder;
    this.maxCacheSize = maxCacheSize;
  }

  public IdentifierEncoder() {
    this(options -> {});
  }

  public IdentifierEncoder(Consumer<MessageEncoder.IdentifierOptions> consumer) {
    var options = new Options();
    consumer.accept(options);
    charsetEncoder =
        options.charsetEncoder != null
            ? options.charsetEncoder
            : StandardCharsets.UTF_8.newEncoder();
    maxCacheSize = options.maxCacheSize;
  }

  @Override
  public void encode(String value, MessageSink sink, MessageWriter writer) throws IOException {
    var bytes = cache.computeIfAbsent(value, (string) -> doEncode(string, sink));
    if (cacheSize > maxCacheSize) {
      // not optimizing for this case, just don't want to fail hard
      cache.clear();
      cacheSize = 0;
    }
    writer.writeStringHeader(bytes.length);
    sink.write(bytes);
  }

  private byte[] doEncode(String value, MessageSink sink) {
    var charBuffer = CharBuffer.wrap(value);
    var byteBuffer =
        sink.allocator()
            .acquireByteBuffer(value.length() * (long) charsetEncoder.maxBytesPerChar());
    try {
      var result = charsetEncoder.encode(charBuffer, byteBuffer, true);
      if (result.isError()) throw Exceptions.stringEncodingError(result, charBuffer);
      byteBuffer.flip();
      var bytes = new byte[byteBuffer.remaining()];
      byteBuffer.get(bytes);
      cacheSize += bytes.length;
      return bytes;
    } finally {
      sink.allocator().release(byteBuffer);
    }
  }
}
