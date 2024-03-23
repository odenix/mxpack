/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.EOFException;
import java.io.IOException;
import org.translatenix.minipack.ReaderException;
import org.translatenix.minipack.ValueType;
import org.translatenix.minipack.WriterException;

/** Exceptions thrown by the minipack library. */
public final class Exceptions {
  private Exceptions() {}

  public static IllegalArgumentException arrayBackedBufferRequired() {
    return new IllegalArgumentException(
        "This message source/sink requires a ByteBuffer backed by an accessible array"
            + " (buffer.hasArray()).");
  }

  public static ReaderException stringTooLarge(int requestedCapacity, int maxCapacity) {
    // tailor message to current use of BufferAllocator
    return new ReaderException(
        "MessagePack string has a length of "
            + requestedCapacity
            + " bytes, exceeding the maximum allowed length of "
            + maxCapacity
            + " bytes.");
  }

  public static EOFException prematureEndOfInput(int minBytes, int bytesRead) {
    return new EOFException(
        "Expected at least "
            + minBytes
            + " more bytes, but reached end of input after "
            + bytesRead
            + " bytes.");
  }

  public static IllegalArgumentException invalidCapacityLimits(int minCapacity, int maxCapacity) {
    return new IllegalArgumentException(
        "Invalid allocator capacity limits: minCapacity="
            + minCapacity
            + ", maxCapacity="
            + maxCapacity);
  }

  public static IllegalStateException sourceRequired() {
    return new IllegalStateException("MessageReader.Builder.source() must be set.");
  }

  public static IllegalStateException bufferTooSmall(int capacity, int minCapacity) {
    return new IllegalStateException(
        "The minimum supported ByteBuffer capacity is " + minCapacity + ", but got: " + capacity);
  }

  public static ReaderException typeMismatch(byte format, RequestedType requestedType) {
    return new ReaderException(
        "Type mismatch: MessagePack value of type "
            + ValueFormat.toType(format)
            + " cannot be read as type "
            + requestedType
            + ".");
  }

  public static ReaderException integerOverflow(long value, RequestedType requestedType) {
    return new ReaderException(
        "Integer overflow: MessagePack value "
            + value
            + " does not fit into type "
            + requestedType
            + ".");
  }

  public static ReaderException invalidValueFormat(byte format) {
    return new ReaderException("Invalid MessagePack value format: " + format);
  }

  public static ReaderException ioErrorReadingFromSource(IOException e) {
    return new ReaderException("I/O error reading from message source.", e);
  }

  public static ReaderException lengthTooLarge(int length, ValueType valueType) {
    return new ReaderException(
        "MessagePack value of type "
            + valueType
            + " has a length of "
            + length
            + " bytes, exceeding the maximum supported length 2^31-1 (Integer.MAX_VALUE).");
  }

  public static WriterException ioErrorClosingSource(IOException e) {
    return new WriterException("I/O error closing message source.", e);
  }

  public static IllegalStateException sinkRequired() {
    return new IllegalStateException("MessageWriter.Builder.sink() must be set.");
  }

  public static WriterException invalidSurrogatePair(int position) {
    return new WriterException(
        "Refusing to write string with invalid surrogate pair at position " + position + ".");
  }

  public static WriterException ioErrorWritingToSink(IOException e) {
    return new WriterException("I/O error writing to message sink.", e);
  }

  public static WriterException ioErrorFlushingSink(IOException e) {
    return new WriterException("I/O error flushing message sink.", e);
  }

  public static WriterException ioErrorClosingSink(IOException e) {
    return new WriterException("I/O error closing message sink.", e);
  }

  public static IllegalArgumentException invalidLength(int length) {
    return new IllegalArgumentException("TODO");
  }

  public static IllegalArgumentException invalidExtensionType(byte type) {
    return new IllegalArgumentException("TODO");
  }
}
