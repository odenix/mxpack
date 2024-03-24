/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.Nullable;
import org.translatenix.minipack.MessageSource;
import org.translatenix.minipack.StringReader;

public final class Utf8StringReader implements StringReader<String> {
  private final GrowableBuffer growableBuffer;

  public Utf8StringReader(int stringSizeLimit) {
    growableBuffer = new GrowableBuffer(1024, stringSizeLimit);
  }

  @Override
  public String read(ByteBuffer buffer, MessageSource source) throws IOException {
    source.ensureRemaining(1, buffer);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.STR8 -> {
        source.ensureRemaining(1, buffer);
        yield read(buffer, buffer.get() & 0xff, source);
      }
      case ValueFormat.STR16 -> {
        source.ensureRemaining(2, buffer);
        yield read(buffer, buffer.getShort() & 0xffff, source);
      }
      case ValueFormat.STR32 -> {
        source.ensureRemaining(4, buffer);
        var length = buffer.getInt();
        if (length < 0) {
          throw Exceptions.stringTooLarge(length & 0xffffffffL, Integer.MAX_VALUE);
        }
        yield read(buffer, length, source);
      }
      default -> {
        if (ValueFormat.isFixStr(format)) {
          yield read(buffer, ValueFormat.getFixStrLength(format), source);
        } else {
          throw Exceptions.typeMismatch(format, RequestedType.STRING);
        }
      }
    };
  }

  private String read(ByteBuffer buffer, int length, MessageSource source) throws IOException {
    if (buffer.hasArray() && length <= buffer.capacity()) {
      source.ensureRemaining(length, buffer);
      var result = convertToString(buffer, length);
      buffer.position(buffer.position() + length);
      return result;
    }
    var utf8Buffer = growableBuffer.get(length).position(0).limit(length);
    var transferLength = Math.min(length, buffer.remaining());
    utf8Buffer.put(0, buffer, buffer.position(), transferLength);
    if (transferLength < length) {
      utf8Buffer.position(transferLength);
      source.readAtLeast(utf8Buffer, utf8Buffer.remaining());
      utf8Buffer.position(0);
    }
    buffer.position(buffer.position() + transferLength);
    return convertToString(utf8Buffer, length);
  }

  private String convertToString(ByteBuffer buffer, int length) {
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
        if (requestedCapacity > maxCapacity) {
          throw Exceptions.stringTooLarge(requestedCapacity, maxCapacity);
        }
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
