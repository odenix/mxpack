/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.minipack.core.internal.*;

/** The underlying source of a {@link MessageReader}. */
public abstract class MessageSource implements Closeable {
  private static final int MIN_BUFFER_CAPACITY = 9; // MessageFormat + long/double
  private static final int DEFAULT_BUFFER_CAPACITY = 1024 * 8;

  protected final BufferAllocator allocator;
  protected final ByteBuffer buffer;

  public static MessageSource of(InputStream stream, BufferAllocator allocator) {
    return new InputStreamSource(stream, allocator);
  }

  public static MessageSource of(
      InputStream stream, BufferAllocator allocator, int bufferCapacity) {
    return new InputStreamSource(stream, allocator, bufferCapacity);
  }

  public static MessageSource of(ReadableByteChannel blockingChannel, BufferAllocator allocator) {
    return new ChannelSource(blockingChannel, allocator);
  }

  public static MessageSource of(
      ReadableByteChannel blockingChannel, BufferAllocator allocator, int bufferCapacity) {
    return new ChannelSource(blockingChannel, allocator, bufferCapacity);
  }

  public static MessageSource of(ByteBuffer buffer, BufferAllocator allocator) {
    return new ByteBufferSource(buffer, allocator);
  }

  public MessageSource(BufferAllocator allocator) {
    this(allocator, DEFAULT_BUFFER_CAPACITY);
  }

  public MessageSource(BufferAllocator allocator, int bufferCapacity) {
    this(allocator, allocator.byteBuffer(bufferCapacity).limit(0));
  }

  public MessageSource(BufferAllocator allocator, ByteBuffer buffer) {
    if (buffer.capacity() < MIN_BUFFER_CAPACITY) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_CAPACITY);
    }
    this.allocator = allocator;
    this.buffer = buffer;
  }

  protected abstract int doRead(ByteBuffer buffer, int minBytesHint) throws IOException;

  protected abstract void doSkip(int length) throws IOException;

  protected abstract void doClose() throws IOException;

  public ByteBuffer buffer() {
    return buffer;
  }

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
  public final int read(ByteBuffer buffer) throws IOException {
    return doRead(buffer, 1);
  }

  /**
   * Reads between {@code minBytes} and {@linkplain ByteBuffer#remaining() remaining} bytes from
   * this source into the given buffer, returning the actual number of bytes read.
   *
   * <p>Throws {@link java.io.EOFException} if the end of input is reached before {@code minBytes}
   * bytes have been read.
   */
  public final int readAtLeast(ByteBuffer buffer, int minBytes) throws IOException {
    assert minBytes <= buffer.remaining();
    var totalBytesRead = 0;
    while (totalBytesRead < minBytes) {
      var bytesRead = doRead(buffer, minBytes);
      if (bytesRead == -1) {
        throw Exceptions.prematureEndOfInput(minBytes, totalBytesRead);
      }
      totalBytesRead += bytesRead;
    }
    return totalBytesRead;
  }

  public long transferTo(WritableByteChannel channel, final long maxBytesToTransfer)
      throws IOException {
    var bytesLeft = maxBytesToTransfer;
    while (bytesLeft > 0) {
      buffer.limit((int) Math.min(bytesLeft, buffer.remaining()));
      var bytesRead = doRead(buffer, 1);
      if (bytesRead == -1) return maxBytesToTransfer - bytesLeft;
      buffer.flip();
      var remaining = buffer.remaining();
      var bytesWritten = channel.write(buffer);
      if (bytesWritten != remaining) {
        throw Exceptions.nonBlockingChannelDetected();
      }
      bytesLeft -= bytesWritten;
      buffer.clear();
    }
    return maxBytesToTransfer;
  }

  /**
   * Reads enough bytes from this source into the given buffer for {@linkplain ByteBuffer#get()
   * getting} at least {@code length} bytes from the buffer.
   *
   * <p>The number of bytes read is between 0 and {@link ByteBuffer#remaining()}.
   */
  public final void ensureRemaining(int length) throws IOException {
    var minBytesToRead = length - buffer.remaining();
    if (minBytesToRead <= 0) return;
    buffer.compact();
    readAtLeast(buffer, minBytesToRead);
    buffer.flip();
  }

  public final void skip(int length) throws IOException {
    var remaining = buffer.remaining();
    if (remaining >= length) {
      buffer.position(buffer.position() + length);
      return;
    }
    doSkip(length - remaining);
    buffer.position(buffer.limit());
  }

  public final byte nextByte() throws IOException {
    ensureRemaining(1);
    return buffer.get(buffer.position());
  }

  public final byte readByte() throws IOException {
    ensureRemaining(1);
    return buffer.get();
  }

  public final byte[] readBytes(int length) throws IOException {
    ensureRemaining(length);
    var bytes = new byte[length];
    buffer.get(bytes);
    return bytes;
  }

  public final short readShort() throws IOException {
    ensureRemaining(2);
    return buffer.getShort();
  }

  public final int readInt() throws IOException {
    ensureRemaining(4);
    return buffer.getInt();
  }

  public final long readLong() throws IOException {
    ensureRemaining(8);
    return buffer.getLong();
  }

  public final float readFloat() throws IOException {
    ensureRemaining(4);
    return buffer.getFloat();
  }

  public final double readDouble() throws IOException {
    ensureRemaining(8);
    return buffer.getDouble();
  }

  public final short readUByte() throws IOException {
    return (short) (readByte() & 0xff);
  }

  public final int readUShort() throws IOException {
    return readShort() & 0xffff;
  }

  public final long readUInt() throws IOException {
    return readInt() & 0xffffffffL;
  }

  public final short readLength8() throws IOException {
    return readUByte();
  }

  public final int readLength16() throws IOException {
    return readUShort();
  }

  public final int readLength32(MessageType type) throws IOException {
    var length = readInt();
    if (length >= 0) return length;
    throw Exceptions.lengthOverflow(length & 0xffffffffL, type);
  }

  @Override
  public final void close() throws IOException {
    try {
      doClose();
    } finally {
      allocator.release(buffer);
    }
  }
}
