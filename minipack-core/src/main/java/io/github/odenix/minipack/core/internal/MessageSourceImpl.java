/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import io.github.odenix.minipack.core.BufferAllocator;
import io.github.odenix.minipack.core.LeasedByteBuffer;
import io.github.odenix.minipack.core.MessageSource;
import io.github.odenix.minipack.core.MessageType;

/// Implementation of [MessageSource].
///
/// Calls [#checkNotClosed()] before every call to [#provider], which is cheap enough.
/// Unfortunately, this won't detect use-after-close bugs related to [#leasedReadBuffer].
/// Such bugs could be ruled out in one of the following ways:
/// * call [LeasedByteBuffer#get()] for every [#readBuffer] operation (expensive)
/// * don't pool [#readBuffer]
public final class MessageSourceImpl extends AbstractCloseable implements MessageSource {
  // current hard minimum is 8 (see readLong() and readDouble())
  static final int MIN_BUFFER_CAPACITY = 16;

  private final Provider provider;
  private final BufferAllocator allocator;
  private final LeasedByteBuffer leasedReadBuffer;
  private final ByteBuffer readBuffer;

  MessageSourceImpl(Provider provider, BufferAllocator allocator, LeasedByteBuffer leasedReadBuffer) {
    this.provider = provider;
    this.allocator = allocator;
    this.leasedReadBuffer = leasedReadBuffer;
    readBuffer = leasedReadBuffer.get();
    if (readBuffer.capacity() < MIN_BUFFER_CAPACITY) {
      throw Exceptions.bufferTooSmall(readBuffer.capacity(), MIN_BUFFER_CAPACITY);
    }
  }

  @Override
  public ByteBuffer buffer() {
    return readBuffer;
  }

  @Override
  public BufferAllocator allocator() {
    return allocator;
  }

  @Override
  public int read(ByteBuffer buffer) throws IOException {
    checkNotClosed();
    return provider.read(buffer, 1);
  }

  @Override
  public int readAtLeast(ByteBuffer buffer, int length) throws IOException {
    if (length > buffer.remaining()) {
      throw Exceptions.remainingBufferTooSmall(buffer.remaining(), length);
    }
    var totalBytesRead = 0;
    while (totalBytesRead < length) {
      checkNotClosed();
      var bytesRead = provider.read(buffer, length);
      if (bytesRead == -1) {
        throw Exceptions.unexpectedEndOfInput(length - totalBytesRead);
      }
      totalBytesRead += bytesRead;
    }
    return totalBytesRead;
  }

  @Override
  public long transferTo(WritableByteChannel channel, long length) throws IOException {
    checkNotClosed();
    return provider.transferTo(channel, length, readBuffer);
  }

  @Override
  public void ensureRemaining(int length) throws IOException {
    var minBytesToRead = length - readBuffer.remaining();
    if (minBytesToRead <= 0) return;
    readBuffer.compact();
    readAtLeast(readBuffer, minBytesToRead);
    readBuffer.flip();
  }

  @Override
  public void skip(int length) throws IOException {
    checkNotClosed();
    provider.skip(length, readBuffer);
  }

  @Override
  public byte nextByte() throws IOException {
    ensureRemaining(1);
    return readBuffer.get(readBuffer.position());
  }

  @Override
  public byte readByte() throws IOException {
    ensureRemaining(1);
    return readBuffer.get();
  }

  @Override
  public short readShort() throws IOException {
    ensureRemaining(2);
    return readBuffer.getShort();
  }

  @Override
  public int readInt() throws IOException {
    ensureRemaining(4);
    return readBuffer.getInt();
  }

  @Override
  public long readLong() throws IOException {
    ensureRemaining(8);
    return readBuffer.getLong();
  }

  @Override
  public float readFloat() throws IOException {
    ensureRemaining(4);
    return readBuffer.getFloat();
  }

  @Override
  public double readDouble() throws IOException {
    ensureRemaining(8);
    return readBuffer.getDouble();
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

  void close() throws IOException {
    if (getAndSetClosed()) return;
    try {
      provider.close();
    } finally {
      leasedReadBuffer.close();
    }
  }
}
