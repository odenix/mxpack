/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.minipack.core.*;

public final class IdentifierEncoder implements Encoder<String> {
  private final ConcurrentMap<String, byte[]> cache = new ConcurrentHashMap<>();
  private final int maxStringSize;
  private final int maxCacheSize;

  public IdentifierEncoder(int maxStringSize, int maxCacheSize) {
    this.maxStringSize = maxStringSize;
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public void encode(String value, ByteBuffer buffer, MessageSink sink, MessageWriter writer)
      throws IOException {
    var bytes =
        cache.computeIfAbsent(
            value,
            (str) -> {
              var b = str.getBytes(StandardCharsets.UTF_8);
              if (b.length > maxStringSize) {
                throw Exceptions.stringTooLargeOnWrite(b.length, maxStringSize);
              }
              if (cache.size() > maxCacheSize) {
                throw Exceptions.identifierCacheSizeExceeded(maxCacheSize);
              }
              return b;
            });
    writer.writeStringHeader(bytes.length);
    sink.putBytes(buffer, bytes);
  }
}
