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
import org.minipack.core.internal.OutputStreamSink;

/** The underlying sink of a {@link MessageWriter}. */
public abstract class MessageSink implements Closeable {
  /** Returns a sink that writes to the given output stream. */
  static MessageSink of(OutputStream stream) {
    return new OutputStreamSink(stream);
  }

  /** Returns a sink that writes to the given blocking channel. */
  static MessageSink of(WritableByteChannel blockingChannel) {
    return new ChannelSink(blockingChannel);
  }

  /**
   * Writes the given buffer's bytes from index 0 to the buffer's current position to this sink,
   * then clears the buffer. Returns the number of bytes written.
   */
  public abstract int write(ByteBuffer buffer) throws IOException;

  /** Flushes this sink. */
  public abstract void flush() throws IOException;

  /**
   * Writes enough bytes from the given buffer to this sink for {@linkplain ByteBuffer#put putting}
   * at least {@code byteCount} bytes into the buffer.
   *
   * <p>The number of bytes written is between 0 and {@linkplain ByteBuffer#remaining() remaining}.
   */
  public final void ensureRemaining(ByteBuffer buffer, int byteCount) throws IOException {
    assert byteCount <= buffer.capacity();
    var minBytes = byteCount - buffer.remaining();
    if (minBytes > 0) {
      buffer.flip();
      write(buffer);
      buffer.clear();
    }
  }

  /**
   * Puts a byte value into the given buffer, ensuring that the buffer has enough space remaining.
   */
  public final void putByte(ByteBuffer buffer, byte value) throws IOException {
    ensureRemaining(buffer, 1);
    buffer.put(value);
  }

  /**
   * Puts two byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void putBytes(ByteBuffer buffer, byte value1, byte value2) throws IOException {
    ensureRemaining(buffer, 2);
    buffer.put(value1);
    buffer.put(value2);
  }

  /**
   * Puts three byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void putBytes(ByteBuffer buffer, byte value1, byte value2, byte value3)
      throws IOException {
    ensureRemaining(buffer, 3);
    buffer.put(value1);
    buffer.put(value2);
    buffer.put(value3);
  }

  /**
   * Puts four byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void putBytes(ByteBuffer buffer, byte value1, byte value2, byte value3, byte value4)
      throws IOException {
    ensureRemaining(buffer, 4);
    buffer.put(value1);
    buffer.put(value2);
    buffer.put(value3);
    buffer.put(value4);
  }

  public final void putBytes(ByteBuffer buffer, byte[] values) throws IOException {
    ensureRemaining(buffer, values.length);
    buffer.put(values);
  }

  public final void putShort(ByteBuffer buffer, short value) throws IOException {
    ensureRemaining(buffer, 2);
    buffer.putShort(value);
  }

  public final void putInt(ByteBuffer buffer, int value) throws IOException {
    ensureRemaining(buffer, 4);
    buffer.putInt(value);
  }

  public final void putLong(ByteBuffer buffer, long value) throws IOException {
    ensureRemaining(buffer, 8);
    buffer.putLong(value);
  }
}
