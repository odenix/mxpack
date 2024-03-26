/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.translatenix.minipack.Decoder;
import org.translatenix.minipack.MessageSource;

public final class IdentifierDecoder implements Decoder<String> {
  private final ConcurrentMap<byte[], String> cache = new ConcurrentHashMap<>();
  private final int maxSize;

  public IdentifierDecoder(int maxSize) {
    this.maxSize = maxSize;
  }

  @Override
  public String decode(ByteBuffer buffer, MessageSource source) throws IOException {
    var length = source.getStringHeader(buffer);
    source.ensureRemaining(length, buffer);
    source.readAtLeast(buffer, length);
    var bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    var string = cache.computeIfAbsent(bytes, (bs) -> new String(bs, StandardCharsets.UTF_8));
    evictIfNecessary();
    return string;
  }

  private void evictIfNecessary() {
    if (cache.size() > maxSize) {
      // evict any entry
      var key = cache.keySet().iterator().next();
      cache.remove(key);
    }
  }
}
