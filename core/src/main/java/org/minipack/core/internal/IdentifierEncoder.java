/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.minipack.core.*;

public final class IdentifierEncoder implements MessageEncoder<String> {
  private final ConcurrentMap<String, byte[]> cache = new ConcurrentHashMap<>();
  private int cacheSize;
  private final int maxCacheSize;

  public IdentifierEncoder(int maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public void encode(String value, MessageSink sink, MessageWriter writer) throws IOException {
    var bytes =
        cache.computeIfAbsent(
            value,
            (str) -> {
              var b = str.getBytes(StandardCharsets.UTF_8);
              cacheSize += b.length;
              if (cacheSize > maxCacheSize) {
                throw Exceptions.identifierCacheSizeExceeded(maxCacheSize);
              }
              return b;
            });
    if (bytes.length > sink.buffer().capacity()) {
      throw Exceptions.identifierTooLarge(bytes.length, sink.buffer().capacity());
    }
    writer.writeStringHeader(bytes.length);
    sink.putBytes(bytes);
  }
}
