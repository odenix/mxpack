/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.minipack.java.BufferAllocator;
import org.minipack.java.MessageDecoder;
import org.minipack.java.MessageReader;
import org.minipack.java.MessageSource;

// Microbenchmarks don't show a clear performance benefit over CharsetStringDecoder,
// but IdentifierDecoder also achieves string deduplication.
public final class IdentifierDecoder implements MessageDecoder<String> {
  private final Map<ByteBuffer, String> cache = new HashMap<>();
  private final CharsetDecoder charsetDecoder;
  private final int maxCacheSize;
  private int cacheSize = 0;

  private static class Options implements MessageDecoder.IdentifierOptions {
    private @Nullable CharsetDecoder charsetDecoder;

    private int maxCacheSize = 1024 * 1024;

    @Override
    public IdentifierOptions charsetDecoder(CharsetDecoder decoder) {
      charsetDecoder = decoder;
      return this;
    }

    @Override
    public Options maxCacheSize(int size) {
      maxCacheSize = size;
      return this;
    }
  }

  public IdentifierDecoder() {
    this(options -> {});
  }

  public IdentifierDecoder(Consumer<IdentifierOptions> consumer) {
    var options = new Options();
    consumer.accept(options);
    charsetDecoder =
        options.charsetDecoder != null
            ? options.charsetDecoder
            : StandardCharsets.UTF_8.newDecoder();
    maxCacheSize = options.maxCacheSize;
  }

  @Override
  public String decode(MessageSource source, MessageReader reader) throws IOException {
    var length = reader.readStringHeader();
    var buffer = source.buffer();
    if (length > buffer.capacity()) {
      throw Exceptions.identifierTooLarge(length, buffer.capacity());
    }
    source.ensureRemaining(length);
    var savedLimit = buffer.limit();
    buffer.limit(buffer.position() + length);
    // can't use computeIfAbsent because buffer needs to be copied before being inserted into map
    var string = cache.get(buffer);
    if (string == null) {
      string = decodeAndCacheIdentifier(buffer, length, source.allocator());
    }
    buffer.position(buffer.limit()).limit(savedLimit);
    return string;
  }

  private String decodeAndCacheIdentifier(
      ByteBuffer buffer, int length, BufferAllocator allocator) {
    var savedPosition = buffer.position();
    var bytes = new byte[length];
    buffer.get(bytes);
    buffer.position(savedPosition);
    var chacheKey = ByteBuffer.wrap(bytes);
    var charBuffer = allocator.acquireCharBuffer(length * charsetDecoder.maxCharsPerByte());
    try {
      charsetDecoder.reset();
      var result = charsetDecoder.decode(buffer, charBuffer, true);
      if (result.isError()) throw Exceptions.stringDecodingError(result, buffer);
      charsetDecoder.flush(charBuffer);
      var string = charBuffer.flip().toString();
      if (cacheSize > maxCacheSize) {
        // not optimizing for this case, just don't want to fail hard
        cache.clear();
        cacheSize = 0;
      }
      cache.put(chacheKey, string);
      cacheSize += length;
      return string;
    } finally {
      allocator.release(charBuffer);
    }
  }
}
