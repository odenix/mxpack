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
import org.minipack.core.Decoder;
import org.minipack.core.MessageSource;

public final class NumericIdDecoder implements Decoder<String> {
  private final Map<Integer, String> cache = new HashMap<>();
  private final byte idExtensionType;
  private final byte refExtensionType;
  private final int maxCacheSize;

  public NumericIdDecoder(byte idExtensionType, byte refExtensionType, int maxCacheSize) {
    this.idExtensionType = idExtensionType;
    this.refExtensionType = refExtensionType;
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public String decode(ByteBuffer buffer, MessageSource source) throws IOException {
    var header = source.getExtensionHeader(buffer);
    if (header.type() == refExtensionType) {
      var id = source.getInt(buffer);
      var string = cache.get(id);
      if (string == null) {
        throw Exceptions.unknownIdentifier(id);
      }
      return string;
    }
    if (header.type() == idExtensionType) {
      var id = source.getInt(buffer);
      var bytes = source.getBytes(buffer, header.length() - 4);
      var string = new String(bytes, StandardCharsets.UTF_8);
      cache.put(id, string);
      if (cache.size() > maxCacheSize) {
        throw Exceptions.identifierCacheSizeExceeded(maxCacheSize);
      }
      return string;
    }
    throw Exceptions.typeMismatch(header.type(), RequestedType.EXTENSION);
  }
}
