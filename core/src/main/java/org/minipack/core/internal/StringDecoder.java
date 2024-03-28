/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.Nullable;
import org.minipack.core.Decoder;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageSource;

public final class StringDecoder implements Decoder<String> {
  private final int maxStringSize;
  private final GrowableBuffer growableBuffer;

  public StringDecoder(int minBufferSize, int maxStringSize) {
    this.maxStringSize = maxStringSize;
    growableBuffer = new GrowableBuffer(minBufferSize, maxStringSize);
  }

  @Override
  public String decode(MessageSource source, MessageReader reader) throws IOException {
    var length = reader.readStringHeader();
    if (length > maxStringSize) {
      throw Exceptions.stringTooLargeOnRead(length, maxStringSize);
    }
    var buffer = source.buffer();
    if (buffer.hasArray() && length <= buffer.capacity()) {
      source.ensureRemaining(length);
      var result = decode(buffer, length);
      buffer.position(buffer.position() + length);
      return result;
    }
    var largeBuffer = growableBuffer.get(length);
    reader.readPayload(largeBuffer);
    largeBuffer.position(0);
    return decode(largeBuffer, length);
  }

  private static String decode(ByteBuffer buffer, int length) {
    return new String(
        buffer.array(), buffer.arrayOffset() + buffer.position(), length, StandardCharsets.UTF_8);
  }

  private static final class GrowableBuffer {
    private final int minCapacity;
    private final int maxCapacity;
    private @Nullable ByteBuffer buffer;

    GrowableBuffer(int minCapacity, int maxCapacity) {
      this.minCapacity = Math.min(minCapacity, maxCapacity);
      this.maxCapacity = maxCapacity;
    }

    ByteBuffer get(int requestedCapacity) {
      if (buffer == null || buffer.capacity() < requestedCapacity) {
        assert requestedCapacity <= maxCapacity;
        var newCapacity =
            buffer == null
                ? Math.max(minCapacity, requestedCapacity)
                : Math.max(buffer.capacity() * 2, requestedCapacity);
        buffer = ByteBuffer.allocate(Math.min(maxCapacity, newCapacity));
      }
      return buffer.position(0).limit(requestedCapacity);
    }
  }
}
