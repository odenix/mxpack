/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.translatenix.minipack.internal.ChannelSink;
import org.translatenix.minipack.internal.Exceptions;
import org.translatenix.minipack.internal.OutputStreamSink;
import org.translatenix.minipack.internal.ValueFormat;

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
   * Writes between 1 and {@linkplain ByteBuffer#remaining() remaining} bytes from the given buffer
   * to this sink, returning the actual number of bytes written.
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
      buffer.compact();
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
   * Puts a short value into the given buffer, ensuring that the buffer has enough space remaining.
   */
  public final void putShort(ByteBuffer buffer, short value) throws IOException {
    ensureRemaining(buffer, 2);
    buffer.putShort(value);
  }

  /**
   * Puts an int value into the given buffer, ensuring that the buffer has enough space remaining.
   */
  public final void putInt(ByteBuffer buffer, int value) throws IOException {
    ensureRemaining(buffer, 4);
    buffer.putInt(value);
  }

  /**
   * Puts a long value into the given buffer, ensuring that the buffer has enough space remaining.
   */
  public final void putLong(ByteBuffer buffer, long value) throws IOException {
    ensureRemaining(buffer, 8);
    buffer.putLong(value);
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

  /**
   * Puts a byte and short value into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void putByteAndShort(ByteBuffer buffer, byte value1, short value2)
      throws IOException {
    ensureRemaining(buffer, 3);
    buffer.put(value1);
    buffer.putShort(value2);
  }

  /**
   * Puts a byte and int value into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void putByteAndInt(ByteBuffer buffer, byte value1, int value2) throws IOException {
    ensureRemaining(buffer, 5);
    buffer.put(value1);
    buffer.putInt(value2);
  }

  /**
   * Puts a byte and long value into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  public final void putByteAndLong(ByteBuffer buffer, byte value1, long value2) throws IOException {
    ensureRemaining(buffer, 9);
    buffer.put(value1);
    buffer.putLong(value2);
  }

  /**
   * Puts a MessagePack string header with the given length into the given buffer, ensuring that the
   * buffer has enough space remaining.
   */
  public final void putStringHeader(int length, ByteBuffer buffer) throws IOException {
    if (length < 0) {
      throw Exceptions.negativeLength(length);
    }
    if (length < (1 << 5)) {
      putByte(buffer, (byte) (ValueFormat.FIXSTR_PREFIX | length));
    } else if (length < (1 << 8)) {
      putBytes(buffer, ValueFormat.STR8, (byte) length);
    } else if (length < (1 << 16)) {
      putByteAndShort(buffer, ValueFormat.STR16, (short) length);
    } else {
      putByteAndInt(buffer, ValueFormat.STR32, length);
    }
  }

  /**
   * Puts a MessagePack binary header with the given length into the given buffer, ensuring that the
   * buffer has enough space remaining.
   */
  public final void putBinaryHeader(int length, ByteBuffer buffer) throws IOException {
    if (length < 0) {
      throw Exceptions.negativeLength(length);
    }
    if (length < (1 << 8)) {
      putBytes(buffer, ValueFormat.BIN8, (byte) length);
    } else if (length < (1 << 16)) {
      putByteAndShort(buffer, ValueFormat.BIN16, (short) length);
    } else {
      putByteAndInt(buffer, ValueFormat.BIN32, length);
    }
  }

  /**
   * Puts a MessagePack extension header with the given length into the given buffer, ensuring that
   * the buffer has enough space remaining.
   */
  public final void putExtensionHeader(int length, byte type, ByteBuffer buffer)
      throws IOException {
    if (length < 0) {
      throw Exceptions.negativeLength(length);
    }
    switch (length) {
      case 1 -> putBytes(buffer, ValueFormat.FIXEXT1, type);
      case 2 -> putBytes(buffer, ValueFormat.FIXEXT2, type);
      case 4 -> putBytes(buffer, ValueFormat.FIXEXT4, type);
      case 8 -> putBytes(buffer, ValueFormat.FIXEXT8, type);
      case 16 -> putBytes(buffer, ValueFormat.FIXEXT16, type);
      default -> {
        if (length < (1 << 8)) {
          putBytes(buffer, ValueFormat.EXT8, (byte) length);
        } else if (length < (1 << 16)) {
          putByteAndShort(buffer, ValueFormat.EXT16, (short) length);
        } else {
          putByteAndInt(buffer, ValueFormat.EXT32, length);
        }
        putByte(buffer, type);
      }
    }
  }
}
