/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.minipack.core.MessageEncoder;
import org.minipack.core.MessageSink;
import org.minipack.core.MessageWriter;

public final class IdentifierEncoder implements MessageEncoder<String> {
  private final Map<String, byte[]> cache = new HashMap<>();
  private final int maxCacheSize;
  private int cacheSize = 0;

  public IdentifierEncoder(int maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public void encode(String value, MessageSink sink, MessageWriter writer) throws IOException {
    var bytes =
        cache.computeIfAbsent(
            value,
            (str) -> {
              // TODO: use CharsetEncoder for correctness
              var b = str.getBytes(StandardCharsets.UTF_8);
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
