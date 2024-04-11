/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageDecoder;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageSource;

// Microbenchmarks don't show a clear performance benefit over CharsetStringDecoder,
// but IdentifierDecoder also achieves string deduplication.
public final class IdentifierDecoder implements MessageDecoder<String> {
  private final Map<ByteBuffer, String> cache = new HashMap<>();
  private final CharsetDecoder charsetDecoder;
  private final int maxCacheSize;
  private int cacheSize = 0;

  public IdentifierDecoder(CharsetDecoder charsetDecoder, int maxCacheSize) {
    this.charsetDecoder = charsetDecoder;
    this.maxCacheSize = maxCacheSize;
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
    var charBuffer = allocator.charBuffer(length * charsetDecoder.maxCharsPerByte());
    try {
      charsetDecoder.reset();
      var result = charsetDecoder.decode(buffer, charBuffer, true);
      if (result.isError()) throw Exceptions.codingError(result, 0);
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
