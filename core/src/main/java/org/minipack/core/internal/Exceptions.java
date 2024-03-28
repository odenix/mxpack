/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.EOFException;
import org.minipack.core.ReaderException;
import org.minipack.core.ValueType;
import org.minipack.core.WriterException;

/** Factory methods for exceptions thrown by the minipack library. */
public final class Exceptions {
  private Exceptions() {}

  public static IllegalArgumentException arrayBackedBufferRequired() {
    return new IllegalArgumentException(
        "This message source/sink requires a byte buffer backed by an accessible array"
            + " (ByteBuffer.hasArray()).");
  }

  public static ReaderException stringTooLargeOnRead(long actualSize, int maxSize) {
    return new ReaderException(
        "MessagePack string size "
            + actualSize
            + " exceeds the maximum allowed size of "
            + maxSize
            + " bytes.");
  }

  public static WriterException stringTooLargeOnWrite(long actualSize, int maxSize) {
    return new WriterException(
        "MessagePack string size "
            + actualSize
            + " exceeds the maximum allowed size of "
            + maxSize
            + " bytes.");
  }

  public static ReaderException identifierTooLargeOnRead(long actualSize, int maxSize) {
    return new ReaderException(
        "MessagePack identifier size "
            + actualSize
            + " exceeds the maximum allowed size of "
            + maxSize
            + " bytes.");
  }

  public static WriterException identifierTooLargeOnWrite(long actualSize, int maxSize) {
    return new WriterException(
        "MessagePack identifier size "
            + actualSize
            + " exceeds the maximum allowed size of "
            + maxSize
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

  public static IllegalStateException sourceRequired() {
    return new IllegalStateException("MessageReader.Builder.source() must be set.");
  }

  public static IllegalStateException bufferTooSmall(int capacity, int minCapacity) {
    return new IllegalStateException(
        "ByteBuffer capacity "
            + capacity
            + " is less than the minimum required capacity "
            + minCapacity
            + ".");
  }

  public static ReaderException typeMismatch(byte format, RequestedType requestedType) {
    return new ReaderException(
        "Type mismatch: MessagePack value of type "
            + ValueFormat.toType(format)
            + " cannot be read as type "
            + requestedType
            + ".");
  }

  public static ReaderException timestampTypeMismatch(byte actualType) {
    return new ReaderException(
        "Type mismatch: MessagePack extension value of type "
            + actualType
            + " cannot be read as timestamp.");
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

  public static ReaderException lengthOverflow(long length, ValueType valueType) {
    return new ReaderException(
        "MessagePack value of type "
            + valueType
            + " has a length of "
            + length
            + ", exceeding the maximum supported length 2^31-1 (Integer.MAX_VALUE).");
  }

  public static IllegalStateException sinkRequired() {
    return new IllegalStateException("MessageWriter.Builder.sink() must be set.");
  }

  public static WriterException invalidSurrogatePair(int position) {
    return new WriterException(
        "Refusing to write string with invalid surrogate pair at position " + position + ".");
  }

  public static IllegalArgumentException negativeLength(int length) {
    return new IllegalArgumentException(
        "Expected a positive length value, but got " + length + ".");
  }

  public static WriterException nonBlockingWriteableChannel() {
    return new WriterException(
        "Detected a *non-blocking* WritableByteChannel, which is not supported by this message"
            + " sink.");
  }

  public static ReaderException nonBlockingReadableChannel() {
    return new ReaderException(
        "Detected a *non-blocking* ReadableByteChannel, which is not supported by this message"
            + " source.");
  }

  public static ReaderException unknownIdentifier(int id) {
    return new ReaderException(
        "Peer sent identifier reference with unknown numeric ID " + id + ".");
  }

  public static ReaderException identifierCacheSizeExceededOnRead(int maximumSize) {
    return new ReaderException(
        "Identifier cache size has exceeded maximum allowed size " + maximumSize + ".");
  }

  public static WriterException identifierCacheSizeExceededOnWrite(int maximumSize) {
    return new WriterException(
        "Identifier cache size has exceeded maximum allowed size " + maximumSize + ".");
  }

  public static ReaderException invalidTimestampLength(int length) {
    return new ReaderException("Encountered Timestamp value with invalid length " + length + ".");
  }

  public static ReaderException payloadBufferTooSmall(int expectedSize, int actualSize) {
    return new ReaderException(
        "Expected a payload buffer with at least "
            + expectedSize
            + " remaining bytes, but got a buffer with "
            + actualSize
            + " remaining bytes.");
  }
}
