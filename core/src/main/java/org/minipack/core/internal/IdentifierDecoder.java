/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.minipack.core.Decoder;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageSource;

public final class IdentifierDecoder implements Decoder<String> {
  private final ConcurrentMap<byte[], String> cache = new ConcurrentHashMap<>();
  private int cacheSize;
  private final int maxCacheSize;

  public IdentifierDecoder(int maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public String decode(MessageSource source, MessageReader reader) throws IOException {
    var length = reader.readStringHeader();
    if (length > source.buffer().capacity()) {
      throw Exceptions.identifierTooLargeOnRead(length, source.buffer().capacity());
    }
    var bytes = source.getBytes(length);
    return cache.computeIfAbsent(
        bytes,
        (b) -> {
          cacheSize += b.length;
          if (cacheSize > maxCacheSize) {
            throw Exceptions.identifierCacheSizeExceededOnRead(maxCacheSize);
          }
          return new String(b, StandardCharsets.UTF_8);
        });
  }
}
