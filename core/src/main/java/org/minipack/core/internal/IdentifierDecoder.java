/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.minipack.core.MessageDecoder;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageSource;

public final class IdentifierDecoder implements MessageDecoder<String> {
  // caching also deduplicates strings
  private final Map<ByteBuffer, String> cache = new HashMap<>();
  private final int maxCacheSize;
  private int cacheSize = 0;

  public IdentifierDecoder(int maxCacheSize) {
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
    // temporarily change limit for cache lookup
    var savedLimit = buffer.limit();
    buffer.limit(buffer.position() + length);
    // can't use computeIfAbsent because buffer needs to be copied before being cached
    var string = cache.get(buffer);
    buffer.limit(savedLimit);
    if (string == null) {
      var bytes = new byte[length];
      buffer.get(bytes);
      // TODO: use CharsetDecoder for correctness
      string = new String(bytes, StandardCharsets.UTF_8);
      if (cacheSize > maxCacheSize) {
        // not optimizing for this case, just don't want to fail hard
        cache.clear();
        cacheSize = 0;
      }
      cache.put(ByteBuffer.wrap(bytes), string);
      cacheSize += length;
    } else {
      buffer.position(buffer.position() + length);
    }
    return string;
  }
}
