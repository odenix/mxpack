/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;

public class ReaderException extends RuntimeException {
  private ReaderException(String message) {
    super(message);
  }

  private ReaderException(Throwable cause) {
    super(cause);
  }

  private ReaderException(String message, Throwable cause) {
    super(message, cause);
  }

  static ReaderException wrongFormat(byte format, JavaType javaType) {
    return new ReaderException(
        "MessagePack type `"
            + Format.toType(format)
            + "` cannot be read as Java type `"
            + javaType
            + "`.");
  }

  static ReaderException overflow(long value, byte format, JavaType javaType) {
    return new ReaderException(
        "Value `" + value + "` does not fit into Java type `" + javaType + "`.");
  }

  static ReaderException bufferTooSmall(int capacity, int minCapacity) {
    return new ReaderException(
        "The minimum supported ByteBuffer capacity is " + minCapacity + ", but got: " + capacity);
  }

  static ReaderException stringTooLarge(long length, int maxLength) {
    return new ReaderException(
        "The maximum supported String length is " + maxLength + ", but got: " + length);
  }

  static ReaderException tooLargeBinary(long length) {
    return new ReaderException("TODO");
  }

  static IllegalStateException sourceRequired() {
    return new IllegalStateException("MessageReader.Builder.source() must be set.");
  }

  static IllegalArgumentException invalidAllocatorCapacitu(int minCapacity, int maxCapacity) {
    return new IllegalArgumentException(
        "Invalid allocator capacity: minCapacity=" + minCapacity + ", maxCapacity=" + maxCapacity);
  }

  static ReaderException invalidFormat(byte format) {
    return new ReaderException("Invalid MessagePack format: " + format);
  }

  static ReaderException ioErrorReadingFromSource(IOException e) {
    return new ReaderException("I/O error reading from message source.", e);
  }
}
