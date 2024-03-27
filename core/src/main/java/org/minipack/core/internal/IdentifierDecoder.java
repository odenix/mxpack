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
import org.minipack.core.Decoder;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageSource;

public final class IdentifierDecoder implements Decoder<String> {
  private final ConcurrentMap<byte[], String> cache = new ConcurrentHashMap<>();
  private final int maxCacheSize;

  public IdentifierDecoder(int maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public String decode(ByteBuffer buffer, MessageSource source, MessageReader reader)
      throws IOException {
    var length = reader.readStringHeader();
    var bytes = source.getBytes(buffer, length);
    var string = cache.computeIfAbsent(bytes, (b) -> new String(b, StandardCharsets.UTF_8));
    evictIfNecessary();
    return string;
  }

  private void evictIfNecessary() {
    if (cache.size() > maxCacheSize) {
      // evict any entry
      var key = cache.keySet().iterator().next();
      cache.remove(key);
    }
  }
}
