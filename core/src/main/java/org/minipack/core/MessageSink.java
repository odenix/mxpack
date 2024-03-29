/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.minipack.core.internal.ChannelSink;
import org.minipack.core.internal.Exceptions;
import org.minipack.core.internal.OutputStreamSink;

/** The underlying sink of a {@link MessageWriter}. */
public abstract class MessageSink implements Closeable {
  private static final int MIN_BUFFER_SIZE = 9;

  private final ByteBuffer buffer;

  protected MessageSink(ByteBuffer buffer) {
    if (buffer.capacity() < MIN_BUFFER_SIZE) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_SIZE);
    }
    this.buffer = buffer.position(0).limit(buffer.capacity());
  }

  public ByteBuffer buffer() {
    return buffer;
  }

  /** Returns a sink that writes to the given output stream. */
  static MessageSink of(OutputStream stream) {
    return new OutputStreamSink(stream);
  }

  /** Returns a sink that writes to the given output stream. */
  static MessageSink of(OutputStream stream, ByteBuffer buffer) {
    return new OutputStreamSink(stream, buffer);
  }

  /** Returns a sink that writes to the given blocking channel. */
  static MessageSink of(WritableByteChannel blockingChannel) {
    return new ChannelSink(blockingChannel);
  }

  /** Returns a sink that writes to the given blocking channel. */
  static MessageSink of(WritableByteChannel blockingChannel, ByteBuffer buffer) {
    return new ChannelSink(blockingChannel, buffer);
  }

  /**
   * Writes the given buffer's bytes from index 0 to the buffer's current position to this sink,
   * then clears the buffer. Returns the number of bytes written.
   */
  public abstract int write(ByteBuffer buffer) throws IOException;

  /** Flushes this sink. */
  public abstract void flush() throws IOException;

  public void flushBuffer() throws IOException {
    buffer.flip();
    write(buffer);
    buffer.clear();
  }

  /**
   * Writes enough bytes from the given buffer to this sink for {@linkplain ByteBuffer#put putting}
   * at least {@code byteCount} bytes into the buffer.
   *
   * <p>The number of bytes written is between 0 and {@linkplain ByteBuffer#remaining() remaining}.
   */
  public final void ensureRemaining(int byteCount) throws IOException {
    assert byteCount <= buffer.capacity();
    if (byteCount > buffer.remaining()) flushBuffer();
  }

  /**
   * Puts a byte value into the given buffer, ensuring that the buffer has enough space remaining.
   */
  public final void writeByte(byte value) throws IOException {
    ensureRemaining(1);
    buffer.put(value);
  }

  /**
   * Puts two byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void writeBytes(byte value1, byte value2) throws IOException {
    ensureRemaining(2);
    buffer.put(value1);
    buffer.put(value2);
  }

  /**
   * Puts three byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void writeBytes(byte value1, byte value2, byte value3) throws IOException {
    ensureRemaining(3);
    buffer.put(value1);
    buffer.put(value2);
    buffer.put(value3);
  }

  /**
   * Puts four byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void writeBytes(byte value1, byte value2, byte value3, byte value4)
      throws IOException {
    ensureRemaining(4);
    buffer.put(value1);
    buffer.put(value2);
    buffer.put(value3);
    buffer.put(value4);
  }

  public final void writeBytes(byte[] values) throws IOException {
    ensureRemaining(values.length);
    buffer.put(values);
  }

  public final void writeShort(short value) throws IOException {
    ensureRemaining(2);
    buffer.putShort(value);
  }

  public final void writeInt(int value) throws IOException {
    ensureRemaining(4);
    buffer.putInt(value);
  }

  public final void writeLong(long value) throws IOException {
    ensureRemaining(8);
    buffer.putLong(value);
  }

  public final void writeFloat(float value) throws IOException {
    ensureRemaining(4);
    buffer.putFloat(value);
  }

  public final void writeDouble(double value) throws IOException {
    ensureRemaining(8);
    buffer.putDouble(value);
  }

  public void writeByteAndShort(byte value1, short value2) throws IOException {
    ensureRemaining(3);
    buffer.put(value1);
    buffer.putShort(value2);
  }

  public void writeByteAndInt(byte value1, int value2) throws IOException {
    ensureRemaining(5);
    buffer.put(value1);
    buffer.putInt(value2);
  }

  public void writeByteAndLong(byte value1, long value2) throws IOException {
    ensureRemaining(9);
    buffer.put(value1);
    buffer.putLong(value2);
  }

  public void writeByteAndFloat(byte value1, float value2) throws IOException {
    ensureRemaining(5);
    buffer.put(value1);
    buffer.putFloat(value2);
  }

  public void writeByteAndDouble(byte value1, double value2) throws IOException {
    ensureRemaining(9);
    buffer.put(value1);
    buffer.putDouble(value2);
  }
}
