/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.internal;

import static io.github.odenix.minipack.core.MiniPackException.*;

import java.io.EOFException;
import io.github.odenix.minipack.core.MessageType;

/// Factory methods for exceptions thrown by MiniPack.
public final class Exceptions {
  private Exceptions() {}

  public static SizeLimitExceeded lengthOverflow(long length, MessageType messageType) {
    return new SizeLimitExceeded(
        "MessagePack value of type "
            + messageType
            + " has a length of "
            + length
            + ", exceeding the maximum supported length 2^31-1 (Integer.MAX_VALUE).");
  }

  public static SizeLimitExceeded bufferSizeLimitExceeded(long requestedCapacity, int maxCapacity) {
    return new SizeLimitExceeded(
        "Requested byte/char buffer capacity "
            + requestedCapacity
            + " exceeds the maximum allowed capacity "
            + maxCapacity
            + ".");
  }

  public static TypeMismatch typeMismatch(byte actualFormat, RequestedType requestedType) {
    return new TypeMismatch(
        "MessagePack value of type "
            + MessageFormat.toType(actualFormat)
            + " is incompatible with requested type "
            + requestedType
            + ".");
  }

  public static TypeMismatch timestampTypeMismatch(byte actualType) {
    return new TypeMismatch(
        "MessagePack extension value of type "
            + actualType
            + " is incompatible with requested type -1 (timestamp).");
  }

  public static TypeMismatch integerOverflow(long value, RequestedType requestedType) {
    return new TypeMismatch(
        "MessagePack integer value " + value + " does not fit into type " + requestedType + ".");
  }

  public static InvalidMessageHeader invalidMessageFormat(byte format) {
    return new InvalidMessageHeader("MessagePack value has invalid format " + format + ".");
  }

  public static InvalidMessageHeader invalidTimestampLength(int length) {
    return new InvalidMessageHeader("Timestamp value has invalid length " + length + ".");
  }

  public static EOFException unexpectedEndOfInput(long minBytes) {
    return new EOFException(
        "Expected at least " + minBytes + " more bytes, but reached the end of input.");
  }

  public static IllegalArgumentException negativeArgument(int value) {
    return new IllegalArgumentException("Argument cannot be negative, but got " + value + ".");
  }

  public static IllegalArgumentException bufferTooSmall(int capacity, int requiredCapacity) {
    return new IllegalArgumentException(
        "ByteBuffer capacity "
            + capacity
            + " is less than the minimum required capacity "
            + requiredCapacity
            + ".");
  }

  public static IllegalArgumentException remainingBufferTooSmall(int remaining, int requiredRemaining) {
    return new IllegalArgumentException(
        "The ByteBuffer's remaining number of bytes "
            + remaining
            + " is less than the minimum required value "
            + requiredRemaining
            + ".");
  }

  public static IllegalArgumentException arrayBackedBufferRequired() {
    return new IllegalArgumentException(
        "This message source/sink requires a byte buffer backed by an accessible array"
            + " (ByteBuffer.hasArray()).");
  }

  public static IllegalArgumentException cannotWriteWriteBuffer() {
    return new IllegalArgumentException(
        "MessageSink.buffer() can only be written with flushBuffer().");
  }

  public static IllegalStateException nonBlockingChannelDetected() {
    return new IllegalStateException(
        "Detected a *non-blocking* channel, which is not supported by this message source/sink.");
  }

  public static IllegalStateException outputNotAvailable() {
    return new IllegalStateException("A sink must be closed before obtaining its buffer output.");
  }

  public static IllegalStateException outputAlreadySet() {
    return new IllegalStateException("The buffer output has already been set.");
  }

  public static IllegalStateException alreadyClosed(Object object) {
    return new IllegalStateException("This " + object.getClass().getSimpleName() + " has already been closed.");
  }
}
