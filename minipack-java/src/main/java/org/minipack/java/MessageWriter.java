/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.time.Instant;
import java.util.function.Consumer;
import org.minipack.java.internal.DefaultMessageSink;
import org.minipack.java.internal.DefaultMessageWriter;

/**
 * Writes values encoded in the <a href="https://msgpack.org/">MessagePack</a> binary serialization
 * format.
 *
 * <p>To create a new {@code MessageWriter}, call on of its {@link #of} methods. To write a value,
 * call one of the {@code write()} or {@code writeXYZ()} methods. To flush the underlying
 * {@linkplain DefaultMessageSink sink}, call {@link #flush()}. To close this writer, call {@link
 * #close()}.
 */
public interface MessageWriter extends Closeable {
  interface Options {
    Options stringEncoder(MessageEncoder<CharSequence> encoder);

    Options identifierEncoder(MessageEncoder<? super String> encoder);
  }

  static MessageWriter of(MessageSink sink) {
    return new DefaultMessageWriter(sink);
  }

  static MessageWriter of(MessageSink sink, Consumer<Options> consumer) {
    return new DefaultMessageWriter(sink, consumer);
  }

  /** Writes a nil (null) value. */
  void writeNil() throws IOException;

  /** Writes a boolean value. */
  void write(boolean value) throws IOException;

  /** Writes an integer value that fits into a Java byte. */
  void write(byte value) throws IOException;

  /** Writes an integer value that fits into a Java short. */
  void write(short value) throws IOException;

  /** Writes an integer value that fits into a Java int. */
  void write(int value) throws IOException;

  /** Writes an integer value that fits into a Java long. */
  void write(long value) throws IOException;

  /** Writes a floating point value that fits into a Java float. */
  void write(float value) throws IOException;

  /** Writes a floating point value that fits into a Java double. */
  void write(double value) throws IOException;

  /** Writes a timestamp value. */
  void write(Instant value) throws IOException;

  /** Writes a string value. */
  void write(CharSequence string) throws IOException;

  /** Writes an identifier value. */
  void writeIdentifier(String identifier) throws IOException;

  <T> void write(T value, MessageEncoder<T> encoder) throws IOException;

  /**
   * Starts writing an array value with the given number of elements.
   *
   * <p>A call to this method MUST be followed by {@code elementCount} calls that write the array's
   * elements.
   */
  void writeArrayHeader(int elementCount) throws IOException;

  /**
   * Starts writing a map value with the given number of entries.
   *
   * <p>A call to this method MUST be followed by {@code entryCount*2} calls that alternately write
   * the map's keys and values.
   */
  void writeMapHeader(int entryCount) throws IOException;

  /**
   * Starts writing a string value with the given number of UTF-8 bytes.
   *
   * <p>A call to this method MUST be followed by one or more calls to {@link #writePayload} that
   * write exactly {@code byteCount} bytes in total.
   *
   * <p>This method is a low-level alternative to {@link #write}. It can be useful in the following
   * cases:
   *
   * <ul>
   *   <li>The string to write is already available as UTF-8 byte sequence.
   *   <li>Full control over conversion from {@code java.lang.String} to UTF-8 is required.
   * </ul>
   */
  void writeStringHeader(int length) throws IOException;

  /**
   * Starts writing a binary value with the given number of bytes.
   *
   * <p>A call to this method MUST be followed by one or more calls to {@link #writePayload} that
   * write exactly {@code length} bytes in total.
   */
  void writeBinaryHeader(int length) throws IOException;

  void writeExtensionHeader(int length, byte type) throws IOException;

  void writePayload(ByteBuffer source) throws IOException;

  void writePayloads(ByteBuffer... sources) throws IOException;

  @SuppressWarnings("UnusedReturnValue")
  long writePayload(ReadableByteChannel source, long maxBytes) throws IOException;

  @SuppressWarnings("UnusedReturnValue")
  long writePayload(InputStream source, long maxBytes) throws IOException;

  /**
   * Writes an integer value that fits into a Java byte. The given value is interpreted as unsigned
   * value.
   */
  void writeUnsigned(byte value) throws IOException;

  /**
   * Writes an integer value that fits into a Java short. The given value is interpreted as unsigned
   * value.
   */
  void writeUnsigned(short value) throws IOException;

  /**
   * Writes an integer value that fits into a Java int. The given value is interpreted as unsigned
   * value.
   */
  void writeUnsigned(int value) throws IOException;

  /**
   * Writes an integer value that fits into a Java long. The given value is interpreted as unsigned
   * value.
   */
  void writeUnsigned(long value) throws IOException;

  /**
   * {@linkplain MessageSink#flush() Flushes} the underlying message {@linkplain MessageSink sink}.
   */
  void flush() throws IOException;

  /**
   * {@linkplain MessageSink#close() Closes} the underlying message {@linkplain MessageSink sink}.
   */
  @Override
  void close() throws IOException;
}
