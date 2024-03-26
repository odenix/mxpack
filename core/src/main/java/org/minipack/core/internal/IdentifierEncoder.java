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
  private final int maxCacheSize;

  public IdentifierEncoder(int maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public void encode(String value, ByteBuffer buffer, MessageSink sink) throws IOException {
    var bytes = cache.computeIfAbsent(value, (str) -> str.getBytes(StandardCharsets.UTF_8));
    evictIfNecessary();
    sink.putStringHeader(buffer, bytes.length);
    sink.putBytes(buffer, bytes);
  }

  private void evictIfNecessary() {
    if (cache.size() > maxCacheSize) {
      // evict any entry
      var key = cache.keySet().iterator().next();
      cache.remove(key);
    }
  }
}
