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
import org.minipack.core.Encoder;
import org.minipack.core.MessageSink;

public final class NumericIdEncoder implements Encoder<String> {
  private final Map<String, Integer> cache = new HashMap<>();
  private final byte idExtensionType;
  private final byte refExtensionType;
  private final int maxCacheSize;
  private int nextId = 0;

  public NumericIdEncoder(byte idExtensionType, byte refExtensionType, int maxCacheSize) {
    this.idExtensionType = idExtensionType;
    this.refExtensionType = refExtensionType;
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public void encode(String value, ByteBuffer buffer, MessageSink sink) throws IOException {
    var id = cache.get(value);
    if (id == null) {
      id = nextId++;
      cache.put(value, id);
      if (cache.size() > maxCacheSize) {
        throw Exceptions.identifierCacheSizeExceeded(maxCacheSize);
      }
      var bytes = value.getBytes(StandardCharsets.UTF_8);
      sink.putExtensionHeader(4 + bytes.length, idExtensionType, buffer);
      sink.putInt(buffer, id);
      sink.putBytes(buffer, bytes);
    } else {
      sink.putExtensionHeader(4, refExtensionType, buffer);
      sink.putInt(buffer, id);
    }
  }
}
