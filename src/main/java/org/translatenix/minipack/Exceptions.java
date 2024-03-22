/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/** Exceptions thrown by message readers, writers, sources, sinks, and buffer allocators. */
public final class Exceptions {
  private Exceptions() {}

  /**
   * Creates an exception to be thrown by message sources and sinks that only accept byte buffers
   * backed by an {@linkplain ByteBuffer#hasArray() accessible array}.
   */
  public static IllegalArgumentException arrayBackedBufferRequired() {
    return new IllegalArgumentException(
        "This message source/sink requires a ByteBuffer backed by an accessible array"
            + " (buffer.hasArray()).");
  }

  /**
   * Creates an exception to be thrown when a message source reaches the end of input before it has
   * read the requested minimum number of bytes.
   */
  public static EOFException prematureEndOfInput(int minBytes, int bytesRead) {
    return new EOFException(
        "Expected at least "
            + minBytes
            + " more bytes, but reached end of input after "
            + bytesRead
            + " bytes.");
  }

  /**
   * Creates an exception to be thrown when the requested buffer capacity exceeds an allocator's
   * maximum allowed capacity.
   */
  public static ReaderException maxCapacityExceeded(int requestedCapacity, int maxCapacity) {
    return new ReaderException(
        "Requested buffer capacity "
            + requestedCapacity
            + " exceeds maximum allowed capacity "
            + maxCapacity
            + ".");
  }

  static IllegalArgumentException invalidAllocatorCapacitu(int minCapacity, int maxCapacity) {
    return new IllegalArgumentException(
        "Invalid allocator capacity: minCapacity=" + minCapacity + ", maxCapacity=" + maxCapacity);
  }

  static IllegalStateException sourceRequired() {
    return new IllegalStateException("MessageReader.Builder.source() must be set.");
  }

  static IllegalStateException bufferTooSmall(int capacity, int minCapacity) {
    return new IllegalStateException(
        "The minimum supported ByteBuffer capacity is " + minCapacity + ", but got: " + capacity);
  }

  static ReaderException wrongJavaType(byte format, JavaType javaType) {
    return new ReaderException(
        "MessagePack type `"
            + ValueFormat.toType(format)
            + "` cannot be read as Java type `"
            + javaType
            + "`.");
  }

  static ReaderException integerOverflow(long value, byte format, JavaType javaType) {
    return new ReaderException(
        "Integer value `" + value + "` does not fit into Java type `" + javaType + "`.");
  }

  static ReaderException stringTooLarge(long length, int maxLength) {
    return new ReaderException(
        "The maximum supported String length is " + maxLength + ", but got: " + length);
  }

  static ReaderException binaryTooLarge(long length) {
    return new ReaderException("TODO");
  }

  static ReaderException invalidValueFormat(byte format) {
    return new ReaderException("Invalid MessagePack value format: " + format);
  }

  static ReaderException ioErrorReadingFromSource(IOException e) {
    return new ReaderException("I/O error reading from message source.", e);
  }

  static WriterException ioErrorClosingSource(IOException e) {
    return new WriterException("I/O error closing message source.", e);
  }

  static IllegalStateException sinkRequired() {
    return new IllegalStateException("MessageWriter.Builder.sink() must be set.");
  }

  static WriterException invalidSurrogatePair(int position) {
    return new WriterException(
        "Refusing to write string with invalid surrogate pair at position " + position + ".");
  }

  static WriterException ioErrorWritingToSink(IOException e) {
    return new WriterException("I/O error writing to message sink.", e);
  }

  static WriterException ioErrorFlushingSink(IOException e) {
    return new WriterException("I/O error flushing message sink.", e);
  }

  static WriterException ioErrorClosingSink(IOException e) {
    return new WriterException("I/O error closing message sink.", e);
  }
}
