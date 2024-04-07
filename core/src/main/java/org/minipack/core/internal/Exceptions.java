/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.EOFException;
import java.nio.charset.CoderResult;
import org.minipack.core.MessageType;
import org.minipack.core.MinipackException;

/** Factory methods for exceptions thrown by the minipack library. */
public final class Exceptions {
  private Exceptions() {}

  public static MinipackException.MessageSizeException stringTooLarge(
      long actualSize, int maxSize) {
    return new MinipackException.MessageSizeException(
        "MessagePack string size "
            + actualSize
            + " exceeds the maximum allowed size of "
            + maxSize
            + " bytes.");
  }

  public static MinipackException.MessageSizeException identifierTooLarge(
      long actualSize, int maxSize) {
    return new MinipackException.MessageSizeException(
        "MessagePack identifier size "
            + actualSize
            + " exceeds the maximum allowed size of "
            + maxSize
            + " bytes.");
  }

  public static MinipackException.MessageSizeException lengthOverflow(
      long length, MessageType messageType) {
    return new MinipackException.MessageSizeException(
        "MessagePack value of type "
            + messageType
            + " has a length of "
            + length
            + ", exceeding the maximum supported length 2^31-1 (Integer.MAX_VALUE).");
  }

  public static MinipackException.TypeMismatchException typeMismatch(
      byte format, RequestedType requestedType) {
    return new MinipackException.TypeMismatchException(
        "MessagePack value of type "
            + MessageFormat.toType(format)
            + " cannot be read as type "
            + requestedType
            + ".");
  }

  public static MinipackException.TypeMismatchException timestampTypeMismatch(byte actualType) {
    return new MinipackException.TypeMismatchException(
        "MessagePack extension value of type " + actualType + " cannot be read as timestamp.");
  }

  public static MinipackException.TypeMismatchException integerOverflow(
      long value, RequestedType requestedType) {
    return new MinipackException.TypeMismatchException(
        "MessagePack integer value " + value + " does not fit into type " + requestedType + ".");
  }

  public static MinipackException.InvalidMessageException invalidValueFormat(byte format) {
    return new MinipackException.InvalidMessageException(
        "Invalid MessagePack value format: " + format);
  }

  public static MinipackException.InvalidMessageException invalidTimestampLength(int length) {
    return new MinipackException.InvalidMessageException(
        "Timestamp extension value has invalid length " + length + ".");
  }

  public static EOFException prematureEndOfInput(long minBytes, long bytesRead) {
    return new EOFException(
        "Expected at least "
            + minBytes
            + " more bytes, but reached end of input after "
            + bytesRead
            + " bytes.");
  }

  public static IllegalArgumentException codingError(CoderResult result, int position) {
    if (result.isMalformed()) {
      throw Exceptions.malformedCharacter(position, result.length());
    }
    assert result.isUnmappable();
    throw Exceptions.unmappableCharacter(position, result.length());
  }

  // TODO
  public static IllegalArgumentException malformedCharacter(int position, int length) {
    return new IllegalArgumentException(
        "String value has malformed character of length "
            + length
            + " at position "
            + position
            + ".");
  }

  // TODO
  public static IllegalArgumentException unmappableCharacter(int position, int length) {
    return new IllegalArgumentException(
        "String value has unmappable character of length "
            + length
            + " at position "
            + position
            + ".");
  }

  public static IllegalArgumentException negativeLength(int length) {
    return new IllegalArgumentException(
        "Expected a positive length value, but got " + length + ".");
  }

  public static IllegalArgumentException payloadBufferTooSmall(int expectedSize, int actualSize) {
    return new IllegalArgumentException(
        "Expected a payload buffer with at least "
            + expectedSize
            + " remaining bytes, but got a buffer with "
            + actualSize
            + " remaining bytes.");
  }

  public static IllegalArgumentException bufferTooSmall(int capacity, int minCapacity) {
    return new IllegalArgumentException(
        "ByteBuffer capacity "
            + capacity
            + " is less than the minimum required capacity "
            + minCapacity
            + ".");
  }

  public static IllegalArgumentException arrayBackedBufferRequired() {
    return new IllegalArgumentException(
        "This message source/sink requires a byte buffer backed by an accessible array"
            + " (ByteBuffer.hasArray()).");
  }

  public static IllegalStateException nonBlockingChannelDetected() {
    return new IllegalStateException(
        "Detected a *non-blocking* Channel, which is not supported by this message source/sink.");
  }

  public static IllegalStateException sourceRequired() {
    return new IllegalStateException("MessageReader.Builder.source() must be set.");
  }

  public static IllegalStateException sinkRequired() {
    return new IllegalStateException("MessageWriter.Builder.sink() must be set.");
  }

  public static AssertionError unreachableCode() {
    throw new AssertionError("unreachable code");
  }

  public static MinipackException bufferSizeLimitExceeded(long requestedSize, int maxSize) {
    throw new MinipackException.SizeLimitExceededException(
        "Requested a buffer of size "
            + requestedSize
            + ", but the maximum allowed size is "
            + maxSize);
  }
}
