/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.minipack.core.internal.*;

/** The underlying sink of a {@link MessageWriter}. */
public abstract class MessageSink implements Closeable {
  private static final int MIN_BUFFER_SIZE = 9; // MessageFormat + long/double

  public static MessageSink of(OutputStream stream, BufferAllocator allocator) {
    return new OutputStreamSink(stream, allocator);
  }

  public static MessageSink of(WritableByteChannel channel, BufferAllocator allocator) {
    return new ChannelSink(channel, allocator);
  }

  protected final BufferAllocator allocator;
  private final ByteBuffer buffer;

  protected MessageSink(BufferAllocator allocator) {
    this.allocator = allocator;
    buffer = allocator.byteBuffer(MIN_BUFFER_SIZE);
  }

  public ByteBuffer buffer() {
    return buffer;
  }

  public BufferAllocator allocator() {
    return allocator;
  }

  protected abstract void doWrite(ByteBuffer buffer) throws IOException;

  // TODO: consider changing doWrite protocol to enable sinks to copy
  //  extra buffers to sink buffer instead of writing them immediately
  protected abstract void doWrite(ByteBuffer... buffers) throws IOException;

  protected abstract void doFlush() throws IOException;

  protected abstract void doClose() throws IOException;

  public final void write(ByteBuffer buffer) throws IOException {
    if (buffer == this.buffer) {
      throw new IllegalArgumentException("TODO");
    }
    this.buffer.flip();
    doWrite(this.buffer, buffer);
    this.buffer.clear();
  }

  public final void write(ByteBuffer... buffers) throws IOException {
    var allBuffers = new ByteBuffer[buffers.length + 1];
    allBuffers[0] = buffer;
    for (int i = 0; i < buffers.length; i++) {
      var buf = buffers[i];
      if (buf == buffer) {
        throw new IllegalArgumentException("TODO");
      }
      allBuffers[i + 1] = buf;
    }
    buffer.flip();
    doWrite(allBuffers);
    buffer.clear();
  }

  public long transferFrom(ReadableByteChannel channel, final long maxBytesToTransfer)
      throws IOException {
    var bytesToTransfer = maxBytesToTransfer;
    while (bytesToTransfer > 0) {
      buffer.limit((int) Math.min(bytesToTransfer, buffer.remaining()));
      var bytesRead = channel.read(buffer);
      if (bytesRead == -1) return maxBytesToTransfer - bytesToTransfer;
      if (bytesRead == 0) throw Exceptions.nonBlockingChannelDetected();
      bytesToTransfer -= bytesRead;
      flushBuffer();
    }
    return maxBytesToTransfer;
  }

  public final void flush() throws IOException {
    flushBuffer();
    doFlush();
  }

  @Override
  public final void close() throws IOException {
    try {
      doClose();
    } finally {
      allocator.release(buffer);
    }
  }

  /**
   * Writes enough bytes from the given buffer to this sink for {@linkplain ByteBuffer#put putting}
   * at least {@code byteCount} bytes into the buffer.
   *
   * <p>The number of bytes written is between 0 and {@linkplain ByteBuffer#remaining() remaining}.
   */
  public final void ensureRemaining(int byteCount) throws IOException {
    if (byteCount > buffer.remaining()) {
      if (byteCount > buffer.capacity()) {
        throw new IllegalArgumentException("TODO");
      }
      flushBuffer();
    }
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

  public final void flushBuffer() throws IOException {
    buffer.flip();
    doWrite(buffer);
    buffer.clear();
  }
}
