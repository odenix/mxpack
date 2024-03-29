/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.minipack.core.*;

public final class IdentifierEncoder implements MessageEncoder<String> {
  private final Map<String, byte[]> cache = new HashMap<>();
  private int cacheSize = 0;
  private final int maxCacheSize;

  public IdentifierEncoder(int maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public void encode(String value, MessageSink sink, MessageWriter writer) throws IOException {
    var bytes =
        cache.computeIfAbsent(
            value,
            (val) -> {
              var b = val.getBytes(StandardCharsets.UTF_8);
              if (b.length > sink.buffer().capacity()) {
                throw Exceptions.identifierTooLarge(b.length, sink.buffer().capacity());
              }
              cacheSize += b.length;
              return b;
            });
    if (cacheSize > maxCacheSize) {
      // not optimizing for this case, just don't want to fail hard
      cache.clear();
      cacheSize = 0;
    }
    writer.writeStringHeader(bytes.length);
    sink.writeBytes(bytes);
  }
}
