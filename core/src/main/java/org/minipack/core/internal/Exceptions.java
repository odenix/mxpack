/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import static org.minipack.core.MiniPackException.*;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import org.minipack.core.MessageType;

/** Factory methods for exceptions thrown by the minipack library. */
public final class Exceptions {
  private Exceptions() {}

  public static SizeLimitExceededException identifierTooLarge(
      long identifierLength, int bufferCapacity) {
    return new SizeLimitExceededException(
        "Identifier length "
            + identifierLength
            + " exceeds MessageSource.buffer().capacity() "
            + bufferCapacity
            + ".");
  }

  public static SizeLimitExceededException lengthOverflow(long length, MessageType messageType) {
    return new SizeLimitExceededException(
        "MessagePack value of type "
            + messageType
            + " has a length of "
            + length
            + ", exceeding the maximum supported length 2^31-1 (Integer.MAX_VALUE).");
  }

  public static SizeLimitExceededException bufferSizeLimitExceeded(
      long requestedSize, int maxSize) {
    return new SizeLimitExceededException(
        "Requested byte/char buffer size "
            + requestedSize
            + " exceeds the configured size limit "
            + maxSize);
  }

  public static TypeMismatchException typeMismatch(byte actualFormat, RequestedType requestedType) {
    return new TypeMismatchException(
        "MessagePack value of type "
            + MessageFormat.toType(actualFormat)
            + " is incompatible with the requested type "
            + requestedType
            + ".");
  }

  public static TypeMismatchException timestampTypeMismatch(byte actualType) {
    return new TypeMismatchException(
        "MessagePack extension value of type "
            + actualType
            + " is incompatible with the requested type -1 (timestamp).");
  }

  public static TypeMismatchException integerOverflow(long value, RequestedType requestedType) {
    return new TypeMismatchException(
        "MessagePack integer value " + value + " does not fit into type " + requestedType + ".");
  }

  public static InvalidMessageHeaderException invalidMessageFormat(byte format) {
    return new InvalidMessageHeaderException(
        "MessagePack value has invalid format " + format + ".");
  }

  public static InvalidMessageHeaderException invalidTimestampLength(int length) {
    return new InvalidMessageHeaderException("Timestamp value has invalid length " + length + ".");
  }

  public static EOFException unexpectedEndOfInput(long minBytes) {
    return new EOFException(
        "Expected at least " + minBytes + " more bytes, but reached end of input.");
  }

  public static InvalidStringEncodingException stringEncodingError(
      CoderResult result, CharBuffer buffer) {
    var chars = new char[result.length()];
    buffer.get(buffer.position(), chars);
    var string = Arrays.toString(chars);
    if (result.isMalformed()) {
      return new InvalidStringEncodingException(
          "String encoder encountered malformed character(s) " + string + ".");
    }
    assert result.isUnmappable();
    return new InvalidStringEncodingException(
        "String encoder encountered unmappable character(s) " + string + ".");
  }

  public static InvalidStringEncodingException stringDecodingError(
      CoderResult result, ByteBuffer buffer) {
    var bytes = new byte[result.length()];
    buffer.get(buffer.position(), bytes);
    var string = Arrays.toString(bytes);
    if (result.isMalformed()) {
      return new InvalidStringEncodingException(
          "String decoder encountered malformed character(s) " + string + ".");
    }
    assert result.isUnmappable();
    return new InvalidStringEncodingException(
        "String decoder encountered unmappable character(s) " + string + ".");
  }

  public static IllegalArgumentException negativeLength(int length) {
    return new IllegalArgumentException("Length cannot be negative, but got " + length + ".");
  }

  public static IllegalArgumentException bufferTooSmall(int capacity, int minCapacity) {
    return new IllegalArgumentException(
        "ByteBuffer capacity "
            + capacity
            + " subceeds the minimum required capacity "
            + minCapacity
            + ".");
  }

  public static IllegalArgumentException arrayBackedBufferRequired() {
    return new IllegalArgumentException(
        "This message source/sink requires a byte buffer backed by an accessible array"
            + " (ByteBuffer.hasArray()).");
  }

  public static IllegalArgumentException cannotWriteSinkBuffer() {
    return new IllegalArgumentException(
        "MessageSink.buffer() can only be written with flushBuffer().");
  }

  public static IllegalStateException nonBlockingChannelDetected() {
    return new IllegalStateException(
        "Detected a *non-blocking* channel, which is not supported by this message source/sink.");
  }

  public static IllegalStateException sourceRequired() {
    return new IllegalStateException("MessageReader.Builder.source() must be set.");
  }

  public static IllegalStateException sinkRequired() {
    return new IllegalStateException("MessageWriter.Builder.sink() must be set.");
  }
}
