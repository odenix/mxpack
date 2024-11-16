/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;
import java.util.function.Consumer;
import org.minipack.java.internal.DefaultMessageReader;

/**
 * Reads values encoded in the <a href="https://msgpack.org/">MessagePack</a> binary serialization
 * format.
 *
 * <p>To create a new {@code MessageReader}, call one of its {@link #of} methods. To read a value,
 * call one of the {@code readXYZ()} methods. To determine the next value's type, call {@link
 * #nextType()}. To close this reader, call {@link #close()}.
 */
public interface MessageReader extends Closeable {
  static MessageReader of(MessageSource source) {
    return new DefaultMessageReader(source);
  }

  static MessageReader of(MessageSource source, Consumer<Options> consumer) {
    return new DefaultMessageReader(source, consumer);
  }

  interface Options {
    Options stringDecoder(MessageDecoder<String> decoder);

    Options identifierDecoder(MessageDecoder<String> decoder);
  }

  /** Returns the type of the next value to be read. */
  MessageType nextType() throws IOException;

  void skipValue() throws IOException;

  void skipValue(int count) throws IOException;

  /** Reads a nil (null) value. */
  void readNil() throws IOException;

  /** Reads a boolean value. */
  boolean readBoolean() throws IOException;

  /** Reads an integer value that fits into a Java byte. */
  byte readByte() throws IOException;

  /** Reads an integer value that fits into a Java short. */
  short readShort() throws IOException;

  /** Reads an integer value that fits into a Java int. */
  int readInt() throws IOException;

  /** Reads an integer value that fits into a Java long. */
  long readLong() throws IOException;

  /** Reads a floating point value that fits into a Java float. */
  float readFloat() throws IOException;

  /** Reads a floating point value that fits into a Java double. */
  double readDouble() throws IOException;

  /** Reads a timestamp value. */
  Instant readTimestamp() throws IOException;

  /**
   * Reads a string value.
   *
   * <p>To read a string as a sequence of bytes, use {@link #readStringHeader()} together with
   * {@link #readPayload}.
   */
  String readString() throws IOException;

  String readIdentifier() throws IOException;

  <T> T read(MessageDecoder<T> decoder) throws IOException;

  /**
   * Starts reading an array value.
   *
   * <p>A call to this method <i>must</i> be followed by {@code n} calls that read the array's
   * elements, where {@code n} is the number of array elements returned by this method.
   */
  int readArrayHeader() throws IOException;

  /**
   * Starts reading a map value.
   *
   * <p>A call to this method <i>must</i> be followed by {@code n*2} calls that alternately read the
   * map's keys and values, where {@code n} is the number of map entries returned by this method.
   */
  int readMapHeader() throws IOException;

  /**
   * Starts reading a binary value.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   */
  int readBinaryHeader() throws IOException;

  /**
   * Starts reading a string value as a sequence of bytes.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   *
   * <p>This method is a low-level alternative to {@link #readString()}.
   */
  int readStringHeader() throws IOException;

  /**
   * Starts reading an extension value.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   */
  ExtensionHeader readExtensionHeader() throws IOException;

  void readPayload(ByteBuffer destination) throws IOException;

  @SuppressWarnings("UnusedReturnValue")
  long readPayload(WritableByteChannel destination, long maxBytes) throws IOException;

  @SuppressWarnings("UnusedReturnValue")
  long readPayload(OutputStream destination, long maxBytes) throws IOException;

  /** Reads an integer value that fits into a Java byte interpreted as an unsigned value. */
  byte readUByte() throws IOException;

  /** Reads an integer value that fits into a Java short interpreted as an unsigned value. */
  short readUShort() throws IOException;

  /** Reads an integer value that fits into a Java int interpreted as an unsigned value. */
  int readUInt() throws IOException;

  /** Reads an integer value that fits into a Java long interpreted as an unsigned value. */
  long readULong() throws IOException;

  /** Closes the underlying message {@linkplain MessageSource source}. */
  @Override
  void close() throws IOException;
}
