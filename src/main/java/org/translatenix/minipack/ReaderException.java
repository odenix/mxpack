/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import org.jspecify.annotations.Nullable;

public class ReaderException extends RuntimeException {
  private ReaderException(String message) {
    super(message);
  }

  private ReaderException(Throwable cause) {
    super(cause);
  }

  private ReaderException(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

  public static ReaderException wrongFormat(byte format, JavaType javaType) {
    return new ReaderException(
        "MessagePack type `"
            + Format.toType(format)
            + "` cannot be read as Java type `"
            + javaType
            + "`.");
  }

  public static ReaderException overflow(long value, byte format, JavaType javaType) {
    return new ReaderException(
        "Value `" + value + "` does not fit into Java type `" + javaType + "`.");
  }

  public static ReaderException bufferTooSmall(int capacity, int minCapacity) {
    return new ReaderException(
        "The minimum supported ByteBuffer capacity is " + minCapacity + ", but got: " + capacity);
  }

  public static ReaderException stringTooLarge(long length, int maxLength) {
    return new ReaderException(
        "The maximum supported String length is " + maxLength + ", but got: " + length);
  }

  public static ReaderException prematureEndOfInput(int requiredBytes, int availableBytes) {
    return new ReaderException("Premature end of input while reading from ReaderSource.");
  }

  public static ReaderException ioError(IOException exception) {
    return new ReaderException("I/O error reading MessagePack message.", exception);
  }

  public static ReaderException uncategorized(@Nullable String message, @Nullable Throwable cause) {
    return new ReaderException(message, cause);
  }

  public static ReaderException arrayBackedBufferRequired() {
    return new ReaderException(
        "This method requires a ByteBuffer backed by an accessible array (buffer.hasArray()).");
  }

  public static ReaderException tooLargeBinary(long length) {
    return new ReaderException("TODO");
  }

  static IllegalStateException sourceRequired() {
    return new IllegalStateException("MessageReader.Builder.source() must be set.");
  }

  public static IllegalArgumentException invalidAllocatorCapacitu(
      int minCapacity, int maxCapacity) {
    return new IllegalArgumentException(
        "Invalid allocator capacity: minCapacity=" + minCapacity + ", maxCapacity=" + maxCapacity);
  }

  public static ReaderException invalidFormat(byte format) {
    return new ReaderException("Invalid MessagePack format: " + format);
  }
}
