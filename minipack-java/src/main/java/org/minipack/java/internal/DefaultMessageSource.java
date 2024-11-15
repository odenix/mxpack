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
import org.minipack.java.BufferAllocator;
import org.minipack.java.MessageSource;
import org.minipack.java.MessageType;

/** Default implementation of {@link MessageSource}. */
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
    sourceBuffer = allocator.acquireByteBuffer(options.bufferCapacity);
  }

  public DefaultMessageSource(
      MessageSource.Provider provider, Consumer<Options> consumer, ByteBuffer buffer) {
    this.provider = provider;
    var options = new DefaultOptions();
    consumer.accept(options);
    allocator = options.allocator;
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

  /**
   * Reads between 1 and {@linkplain ByteBuffer#remaining() remaining} bytes from this source into
   * the given buffer, returning the actual number of bytes read.
   *
   * <p>Returns {@code -1} if no more bytes can be read from this source.
   *
   * <p>{@code minBytesHint} indicates the minimum number of bytes that the caller would like to
   * read. However, unlike {@link #readAtLeast}, this method does not guarantee that more than 1
   * byte will be read.
   */
  @Override
  public int read(ByteBuffer buffer) throws IOException {
    return provider.read(buffer, 1);
  }

  /**
   * Reads between {@code minBytes} and {@linkplain ByteBuffer#remaining() remaining} bytes from
   * this source into the given buffer, returning the actual number of bytes read.
   *
   * <p>Throws {@link java.io.EOFException} if the end of input is reached before {@code minBytes}
   * bytes have been read.
   */
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
    var bytesLeft = maxBytesToTransfer;
    for (var remaining = sourceBuffer.remaining(); bytesLeft > sourceBuffer.remaining(); ) {
      var bytesWritten = destination.write(sourceBuffer);
      if (bytesWritten != remaining) {
        throw Exceptions.nonBlockingChannelDetected();
      }
      bytesLeft -= bytesWritten;
      assert bytesLeft > 0;
      sourceBuffer.clear();
      var bytesRead = provider.read(sourceBuffer, 1);
      sourceBuffer.flip();
      if (bytesRead == -1) {
        return maxBytesToTransfer - bytesLeft;
      }
    }
    assert bytesLeft <= sourceBuffer.remaining();
    var savedLimit = sourceBuffer.limit();
    sourceBuffer.limit(sourceBuffer.position() + (int) bytesLeft);
    var bytesWritten = destination.write(sourceBuffer);
    if (bytesWritten != bytesLeft) {
      throw Exceptions.nonBlockingChannelDetected();
    }
    sourceBuffer.limit(savedLimit);
    return maxBytesToTransfer;
  }

  /**
   * Reads enough bytes from this source into the given buffer for {@linkplain ByteBuffer#get()
   * getting} at least {@code length} bytes from the buffer.
   *
   * <p>The number of bytes read is between 0 and {@link ByteBuffer#remaining()}.
   */
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
    var remaining = sourceBuffer.remaining();
    if (remaining >= length) {
      sourceBuffer.position(sourceBuffer.position() + length);
      return;
    }
    provider.skip(length - remaining);
    sourceBuffer.position(sourceBuffer.limit());
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
      allocator.release(sourceBuffer);
    }
  }
}
