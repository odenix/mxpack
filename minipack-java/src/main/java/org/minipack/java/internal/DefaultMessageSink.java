/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Consumer;
import org.minipack.java.BufferAllocator;
import org.minipack.java.MessageSink;

/** Default implementation of {@link MessageSink}. */
public final class DefaultMessageSink<T> implements MessageSink.InMemory<T> {
  private static final int MIN_BUFFER_CAPACITY = 9; // MessageFormat + long/double
  private static final int DEFAULT_BUFFER_CAPACITY = 1024 * 8;

  static final class DefaultOptions implements MessageSink.Options {
    BufferAllocator allocator = BufferAllocator.ofUnpooled();
    int bufferCapacity = DEFAULT_BUFFER_CAPACITY;

    public Options allocator(BufferAllocator allocator) {
      this.allocator = allocator;
      return this;
    }

    public Options bufferCapacity(int capacity) {
      if (capacity < MIN_BUFFER_CAPACITY) {
        throw Exceptions.bufferTooSmall(capacity, MIN_BUFFER_CAPACITY);
      }
      bufferCapacity = capacity;
      return this;
    }
  }

  private final MessageSink.Provider<T> provider;
  private final BufferAllocator allocator;
  private final ByteBuffer sinkBuffer;
  private boolean isClosed;

  public DefaultMessageSink(MessageSink.Provider<T> provider) {
    this(provider, options -> {});
  }

  public DefaultMessageSink(MessageSink.Provider<T> provider, Consumer<Options> consumer) {
    this.provider = provider;
    var options = new DefaultOptions();
    consumer.accept(options);
    allocator = options.allocator;
    sinkBuffer = allocator.acquireByteBuffer(options.bufferCapacity);
  }

  @Override
  public ByteBuffer buffer() {
    return sinkBuffer;
  }

  @Override
  public BufferAllocator allocator() {
    return allocator;
  }

  @Override
  public void write(ByteBuffer buffer) throws IOException {
    if (buffer == sinkBuffer) {
      throw Exceptions.cannotWriteSinkBuffer();
    }
    sinkBuffer.flip();
    provider.write(sinkBuffer, buffer);
    sinkBuffer.clear();
  }

  @Override
  public void write(ByteBuffer... buffers) throws IOException {
    var allBuffers = new ByteBuffer[buffers.length + 1];
    allBuffers[0] = sinkBuffer;
    for (int i = 0; i < buffers.length; i++) {
      var buf = buffers[i];
      if (buf == sinkBuffer) {
        throw Exceptions.cannotWriteSinkBuffer();
      }
      allBuffers[i + 1] = buf;
    }
    sinkBuffer.flip();
    provider.write(allBuffers);
    sinkBuffer.clear();
  }

  @Override
  public long transferFrom(ReadableByteChannel channel, final long maxBytesToTransfer)
      throws IOException {
    var bytesLeft = maxBytesToTransfer;
    while (bytesLeft > 0) {
      var remaining = sinkBuffer.remaining();
      sinkBuffer.limit((int) Math.min(bytesLeft, remaining));
      var bytesRead = channel.read(sinkBuffer);
      if (bytesRead == -1) return maxBytesToTransfer - bytesLeft;
      if (bytesRead == 0 && remaining > 0) throw Exceptions.nonBlockingChannelDetected();
      bytesLeft -= bytesRead;
      flushBuffer();
    }
    return maxBytesToTransfer;
  }

  @Override
  public void flush() throws IOException {
    flushBuffer();
    provider.flush();
  }

  @Override
  public void close() throws IOException {
    if (isClosed) return;
    isClosed = true;
    try {
      provider.close();
    } finally {
      allocator.release(sinkBuffer);
    }
  }

  @Override
  public T output() {
    if (!isClosed) {
      // TODO: move to Exceptions
      throw new IllegalStateException(
          "In-memory sink must be closed before obtaining output buffer.");
    }
    return provider.output();
  }

  /**
   * Writes enough bytes from the given buffer to this sink for {@linkplain ByteBuffer#put putting}
   * at least {@code byteCount} bytes into the buffer.
   *
   * <p>The number of bytes written is between 0 and {@linkplain ByteBuffer#remaining() remaining}.
   */
  @Override
  public void ensureRemaining(int byteCount) throws IOException {
    if (byteCount > sinkBuffer.remaining()) {
      if (byteCount > sinkBuffer.capacity()) {
        throw Exceptions.bufferSizeLimitExceeded(byteCount, sinkBuffer.capacity());
      }
      flushBuffer();
    }
  }

  /**
   * Puts a byte value into the given buffer, ensuring that the buffer has enough space remaining.
   */
  @Override
  public void write(byte value) throws IOException {
    ensureRemaining(1);
    sinkBuffer.put(value);
  }

  /**
   * Puts two byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  @Override
  public void write(byte value1, byte value2) throws IOException {
    ensureRemaining(2);
    sinkBuffer.put(value1);
    sinkBuffer.put(value2);
  }

  /**
   * Puts three byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  @Override
  public void write(byte value1, byte value2, byte value3) throws IOException {
    ensureRemaining(3);
    sinkBuffer.put(value1);
    sinkBuffer.put(value2);
    sinkBuffer.put(value3);
  }

  /**
   * Puts four byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  @Override
  public void write(byte value1, byte value2, byte value3, byte value4) throws IOException {
    ensureRemaining(4);
    sinkBuffer.put(value1);
    sinkBuffer.put(value2);
    sinkBuffer.put(value3);
    sinkBuffer.put(value4);
  }

  @Override
  public void write(byte[] values) throws IOException {
    ensureRemaining(values.length);
    sinkBuffer.put(values);
  }

  @Override
  public void write(short value) throws IOException {
    ensureRemaining(2);
    sinkBuffer.putShort(value);
  }

  @Override
  public void write(int value) throws IOException {
    ensureRemaining(4);
    sinkBuffer.putInt(value);
  }

  @Override
  public void write(long value) throws IOException {
    ensureRemaining(8);
    sinkBuffer.putLong(value);
  }

  @Override
  public void write(float value) throws IOException {
    ensureRemaining(4);
    sinkBuffer.putFloat(value);
  }

  @Override
  public void write(double value) throws IOException {
    ensureRemaining(8);
    sinkBuffer.putDouble(value);
  }

  @Override
  public void write(byte value1, short value2) throws IOException {
    ensureRemaining(3);
    sinkBuffer.put(value1);
    sinkBuffer.putShort(value2);
  }

  @Override
  public void write(byte value1, int value2) throws IOException {
    ensureRemaining(5);
    sinkBuffer.put(value1);
    sinkBuffer.putInt(value2);
  }

  @Override
  public void write(byte value1, long value2) throws IOException {
    ensureRemaining(9);
    sinkBuffer.put(value1);
    sinkBuffer.putLong(value2);
  }

  @Override
  public void write(byte value1, float value2) throws IOException {
    ensureRemaining(5);
    sinkBuffer.put(value1);
    sinkBuffer.putFloat(value2);
  }

  @Override
  public void write(byte value1, double value2) throws IOException {
    ensureRemaining(9);
    sinkBuffer.put(value1);
    sinkBuffer.putDouble(value2);
  }

  @Override
  public void flushBuffer() throws IOException {
    sinkBuffer.flip();
    provider.write(sinkBuffer);
    sinkBuffer.clear();
  }
}