/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.Nullable;
import org.translatenix.minipack.Decoder;
import org.translatenix.minipack.MessageSource;

public final class StringDecoder implements Decoder<String> {
  private final int sizeLimit;
  private final GrowableBuffer growableBuffer;

  public StringDecoder(int sizeLimit) {
    this.sizeLimit = sizeLimit;
    growableBuffer = new GrowableBuffer(sizeLimit);
  }

  @Override
  public String decode(ByteBuffer buffer, MessageSource source) throws IOException {
    var length = source.getStringHeader(buffer);
    if (length > sizeLimit) {
      throw Exceptions.stringTooLargeOnRead(length, sizeLimit);
    }
    return decode(buffer, length, source);
  }

  private String decode(ByteBuffer buffer, int length, MessageSource source) throws IOException {
    if (buffer.hasArray() && length <= buffer.capacity()) {
      source.ensureRemaining(length, buffer);
      var result = decode(buffer, length);
      buffer.position(buffer.position() + length);
      return result;
    }
    var stringBuffer = growableBuffer.get(length).position(0).limit(length);
    var transferLength = Math.min(length, buffer.remaining());
    stringBuffer.put(0, buffer, buffer.position(), transferLength);
    if (transferLength < length) {
      stringBuffer.position(transferLength);
      source.readAtLeast(stringBuffer, stringBuffer.remaining());
      stringBuffer.position(0);
    }
    buffer.position(buffer.position() + transferLength);
    return decode(stringBuffer, length);
  }

  private static String decode(ByteBuffer buffer, int length) {
    return new String(
        buffer.array(), buffer.arrayOffset() + buffer.position(), length, StandardCharsets.UTF_8);
  }

  private static final class GrowableBuffer {
    private static final int DEFAULT_MIN_CAPACITY = 1 << 10; // TODO

    private final int minCapacity;
    private final int maxCapacity;
    private @Nullable ByteBuffer buffer;

    GrowableBuffer(int maxCapacity) {
      this.minCapacity = Math.min(DEFAULT_MIN_CAPACITY, maxCapacity);
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
      return buffer;
    }
  }
}
