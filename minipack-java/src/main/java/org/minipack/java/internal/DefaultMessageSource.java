/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.minipack.java.BufferAllocator;
import org.minipack.java.MessageSource;
import org.minipack.java.MessageType;

/// Default implementation of [MessageSource].
public final class DefaultMessageSource implements MessageSource {
  private static final int MIN_BUFFER_CAPACITY = 9; // MessageFormat + long/double
  private static final int DEFAULT_BUFFER_CAPACITY = 1024 * 8;

  private static final class DefaultOptions implements Options {
    private BufferAllocator allocator = BufferAllocator.ofUnpooled();
    private int bufferCapacity = DEFAULT_BUFFER_CAPACITY;

    @Override
    public Options allocator(BufferAllocator allocator) {
      this.allocator = Objects.requireNonNull(allocator);
      return this;
    }

    @Override
    public Options bufferCapacity(int capacity) {
      if (capacity < MIN_BUFFER_CAPACITY) {
        throw Exceptions.bufferTooSmall(capacity, MIN_BUFFER_CAPACITY);
      }
      bufferCapacity = capacity;
      return this;
    }
  }

  private final MessageSource.Provider provider;
  private final BufferAllocator allocator;
  private final BufferAllocator.@Nullable PooledByteBuffer pooledSourceBuffer;
  private final ByteBuffer sourceBuffer;
  private boolean isClosed;

  public DefaultMessageSource(MessageSource.Provider provider) {
    this(provider, options -> {});
  }

  public DefaultMessageSource(MessageSource.Provider provider, Consumer<Options> consumer) {
    this.provider = provider;
    var options = new DefaultOptions();
    consumer.accept(options);
    allocator = options.allocator;
    pooledSourceBuffer = allocator.getByteBuffer(options.bufferCapacity);
    sourceBuffer = pooledSourceBuffer.value().limit(0);
  }

  public DefaultMessageSource(
      MessageSource.Provider provider,
      Consumer<Options> consumer,
      BufferAllocator.PooledByteBuffer buffer) {
    this.provider = provider;
    var options = new DefaultOptions();
    consumer.accept(options);
    allocator = options.allocator;
    pooledSourceBuffer = buffer;
    sourceBuffer = buffer.value().limit(0);
  }

  public DefaultMessageSource(
      MessageSource.Provider provider, Consumer<Options> consumer, ByteBuffer buffer) {
    this.provider = provider;
    var options = new DefaultOptions();
    consumer.accept(options);
    allocator = options.allocator;
    pooledSourceBuffer = null;
    sourceBuffer = buffer;
  }

  @Override
  public ByteBuffer buffer() {
    return sourceBuffer;
  }

  @Override
  public BufferAllocator allocator() {
    return allocator;
  }

  @Override
  public int read(ByteBuffer buffer) throws IOException {
    return provider.read(buffer, 1);
  }

  @Override
  public int readAtLeast(ByteBuffer buffer, int minBytes) throws IOException {
    assert minBytes <= buffer.remaining();
    var totalBytesRead = 0;
    while (totalBytesRead < minBytes) {
      var bytesRead = provider.read(buffer, minBytes);
      if (bytesRead == -1) {
        throw Exceptions.unexpectedEndOfInput(minBytes - totalBytesRead);
      }
      totalBytesRead += bytesRead;
    }
    return totalBytesRead;
  }

  @Override
  public long transferTo(WritableByteChannel destination, final long maxBytesToTransfer)
      throws IOException {
    return provider.transferTo(destination, maxBytesToTransfer, sourceBuffer);
  }

  @Override
  public void ensureRemaining(int length) throws IOException {
    var minBytesToRead = length - sourceBuffer.remaining();
    if (minBytesToRead <= 0) return;
    sourceBuffer.compact();
    readAtLeast(sourceBuffer, minBytesToRead);
    sourceBuffer.flip();
  }

  @Override
  public void skip(int length) throws IOException {
    provider.skip(length, sourceBuffer);
  }

  @Override
  public byte nextByte() throws IOException {
    ensureRemaining(1);
    return sourceBuffer.get(sourceBuffer.position());
  }

  @Override
  public byte readByte() throws IOException {
    ensureRemaining(1);
    return sourceBuffer.get();
  }

  @Override
  public byte[] readBytes(int length) throws IOException {
    ensureRemaining(length);
    var bytes = new byte[length];
    sourceBuffer.get(bytes);
    return bytes;
  }

  @Override
  public short readShort() throws IOException {
    ensureRemaining(2);
    return sourceBuffer.getShort();
  }

  @Override
  public int readInt() throws IOException {
    ensureRemaining(4);
    return sourceBuffer.getInt();
  }

  @Override
  public long readLong() throws IOException {
    ensureRemaining(8);
    return sourceBuffer.getLong();
  }

  @Override
  public float readFloat() throws IOException {
    ensureRemaining(4);
    return sourceBuffer.getFloat();
  }

  @Override
  public double readDouble() throws IOException {
    ensureRemaining(8);
    return sourceBuffer.getDouble();
  }

  @Override
  public short readUByte() throws IOException {
    return (short) (readByte() & 0xff);
  }

  @Override
  public int readUShort() throws IOException {
    return readShort() & 0xffff;
  }

  @Override
  public long readUInt() throws IOException {
    return readInt() & 0xffffffffL;
  }

  @Override
  public short readLength8() throws IOException {
    return readUByte();
  }

  @Override
  public int readLength16() throws IOException {
    return readUShort();
  }

  @Override
  public int readLength32(MessageType type) throws IOException {
    var length = readInt();
    if (length >= 0) return length;
    throw Exceptions.lengthOverflow(length & 0xffffffffL, type);
  }

  @Override
  public void close() throws IOException {
    if (isClosed) return;
    isClosed = true;
    try {
      provider.close();
    } finally {
      if (pooledSourceBuffer != null) {
        pooledSourceBuffer.close();
      }
    }
  }
}
