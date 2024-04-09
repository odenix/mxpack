/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.extension;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;
import org.minipack.core.MessageEncoder;
import org.minipack.core.MessageSink;
import org.minipack.core.MessageWriter;
import org.minipack.extension.internal.Exceptions;

public final class NumericIdEncoder implements MessageEncoder<String> {
  private static final int MAX_EXT_HEADER_SIZE = 5;

  private final Map<String, Integer> cache = new HashMap<>();
  private final CharsetEncoder charsetEncoder;
  private final byte idExtensionType;
  private final byte refExtensionType;
  private final int maxCacheSize;
  private int nextId = 0;

  public NumericIdEncoder(CharsetEncoder charsetEncoder, byte idExtensionType, byte refExtensionType, int maxCacheSize) {
    this.charsetEncoder = charsetEncoder;
    this.idExtensionType = idExtensionType;
    this.refExtensionType = refExtensionType;
    this.maxCacheSize = maxCacheSize;
  }

  @Override
  public void encode(String value, MessageSink sink, MessageWriter writer) throws IOException {
    var id = cache.get(value);
    if (id != null) {
      writer.writeExtensionHeader(4, refExtensionType);
      sink.writeInt(id);
      return;
    }
    id = nextId++;
    cache.put(value, id);
    if (cache.size() > maxCacheSize) {
      throw Exceptions.identifierCacheSizeExceeded(maxCacheSize);
    }
    var maxByteLength = value.length() * (long) charsetEncoder.maxBytesPerChar();
    var byteBuffer = sink.allocator().byteBuffer(maxByteLength);
    try {

      var result = charsetEncoder.encode(CharBuffer.wrap(value), byteBuffer, true);
      if (result.isError()) throw Exceptions.codingError(result);
      charsetEncoder.flush(byteBuffer);
      writer.writeExtensionHeader(4 + byteBuffer.position(), idExtensionType);
      sink.writeInt(id);
      sink.write(byteBuffer);
    } finally {
      sink.allocator().release(byteBuffer);
    }
  }
}
