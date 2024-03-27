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
  private final int maxStringSize;
  private final int maxCacheSize;

  public IdentifierDecoder(int maxStringSize, int maxCacheSize) {
    this.maxStringSize = maxStringSize;
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public String decode(ByteBuffer buffer, MessageSource source, MessageReader reader)
      throws IOException {
    var length = reader.readStringHeader();
    if (length > maxStringSize) {
      throw Exceptions.stringTooLargeOnRead(length, maxStringSize);
    }
    var bytes = source.getBytes(buffer, length);
    return cache.computeIfAbsent(
        bytes,
        (b) -> {
          var str = new String(b, StandardCharsets.UTF_8);
          if (cache.size() > maxCacheSize) {
            throw Exceptions.identifierCacheSizeExceeded(maxCacheSize);
          }
          return str;
        });
  }
}
