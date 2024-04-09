/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.extension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.minipack.core.MessageDecoder;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageSource;
import org.minipack.extension.internal.Exceptions;

public final class NumericIdDecoder implements MessageDecoder<String> {
  private final Map<Integer, String> cache = new HashMap<>();
  private final byte idExtensionType;
  private final byte refExtensionType;
  private final int maxCacheSize;

  public NumericIdDecoder(int maxCacheSize) {
    this((byte) 0, (byte) 1, maxCacheSize);
  }

  public NumericIdDecoder(byte idExtensionType, byte refExtensionType, int maxCacheSize) {
    this.idExtensionType = idExtensionType;
    this.refExtensionType = refExtensionType;
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public String decode(MessageSource source, MessageReader reader) throws IOException {
    var header = reader.readExtensionHeader();
    if (header.type() == refExtensionType) {
      var id = source.readInt();
      var string = cache.get(id);
      if (string == null) {
        throw Exceptions.unknownIdentifier(id);
      }
      return string;
    }
    if (header.type() == idExtensionType) {
      var id = source.readInt();
      var stringLength = header.length() - 4;
      source.ensureRemaining(stringLength);
      var buffer = source.buffer();
      var string = new String(buffer.array(), buffer.arrayOffset() + buffer.position(), stringLength, StandardCharsets.UTF_8);
      cache.put(id, string);
      if (cache.size() > maxCacheSize) {
        throw Exceptions.identifierCacheSizeExceeded(maxCacheSize);
      }
      return string;
    }
    throw Exceptions.typeMismatch(header.type());
  }
}
