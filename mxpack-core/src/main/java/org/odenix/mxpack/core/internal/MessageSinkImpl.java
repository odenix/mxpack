/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.odenix.mxpack.core.BufferAllocator;
import org.odenix.mxpack.core.LeasedByteBuffer;
import org.odenix.mxpack.core.MessageSink;

/// Implementation of [MessageSink].
///
/// Calls [#checkNotClosed()] before every call to [#provider], which is cheap enough.
/// Unfortunately, this won't detect use-after-close bugs related to [#leasedWriteBuffer].
/// Such bugs could be ruled out in one of the following ways:
/// * call [LeasedByteBuffer#get()] for every [#writeBuffer] operation (expensive)
/// * don't pool [#writeBuffer]
public final class MessageSinkImpl extends AbstractCloseable implements MessageSink {
  // current hard minimum is 9 (see write(byte, long) and write(byte, double))
  static final int MIN_BUFFER_CAPACITY = 16;

  private final Provider provider;
  private final BufferAllocator allocator;
  private final LeasedByteBuffer leasedWriteBuffer;
  private final ByteBuffer writeBuffer;

  MessageSinkImpl(Provider provider, BufferAllocator allocator, LeasedByteBuffer leasedWriteBuffer) {
    this.provider = provider;
    this.allocator = allocator;
    this.leasedWriteBuffer = leasedWriteBuffer;
    writeBuffer = leasedWriteBuffer.get();
    if (writeBuffer.capacity() < MIN_BUFFER_CAPACITY) {
      throw Exceptions.bufferTooSmall(writeBuffer.capacity(), MIN_BUFFER_CAPACITY);
    }
  }

  @Override
  public ByteBuffer buffer() {
    return writeBuffer;
  }

  @Override
  public BufferAllocator allocator() {
    return allocator;
  }

  @Override
  public void write(ByteBuffer buffer) throws IOException {
    if (buffer == writeBuffer) {
      throw Exceptions.cannotWriteWriteBuffer();
    }
    writeBuffer.flip();
    checkNotClosed();
    provider.write(writeBuffer, buffer);
    writeBuffer.clear();
  }

  @Override
  public void write(ByteBuffer... buffers) throws IOException {
    var allBuffers = new ByteBuffer[buffers.length + 1];
    allBuffers[0] = writeBuffer;
    for (int i = 0; i < buffers.length; i++) {
      var buf = buffers[i];
      if (buf == writeBuffer) {
        throw Exceptions.cannotWriteWriteBuffer();
      }
      allBuffers[i + 1] = buf;
    }
    writeBuffer.flip();
    checkNotClosed();
    provider.write(allBuffers);
    writeBuffer.clear();
  }

  @Override
  public long transferFrom(ReadableByteChannel channel, long length) throws IOException {
    writeBuffer.flip();
    checkNotClosed();
    var bytesTransferred = provider.transferFrom(channel, length, writeBuffer);
    writeBuffer.clear();
    return bytesTransferred;
  }

  @Override
  public void flush() throws IOException {
    checkNotClosed();
    doFlushBuffer();
    provider.flush();
  }

  void close() throws IOException {
    if (getAndSetClosed()) return;
    try {
      // leave it to provider.close() to call provider.flush() if necessary
      doFlushBuffer();
    } finally {
      try {
        provider.close();
      } finally {
        leasedWriteBuffer.close();
      }
    }
  }

  @Override
  public void ensureRemaining(int length) throws IOException {
    if (length <= writeBuffer.remaining()) return;
    if (length > writeBuffer.capacity()) {
      throw Exceptions.bufferSizeLimitExceeded(length, writeBuffer.capacity());
    }
    flushBuffer();
  }

  @Override
  public void write(byte value) throws IOException {
    ensureRemaining(1);
    writeBuffer.put(value);
  }

  @Override
  public void write(short value) throws IOException {
    ensureRemaining(2);
    writeBuffer.putShort(value);
  }

  @Override
  public void write(int value) throws IOException {
    ensureRemaining(4);
    writeBuffer.putInt(value);
  }

  @Override
  public void write(long value) throws IOException {
    ensureRemaining(8);
    writeBuffer.putLong(value);
  }

  @Override
  public void write(float value) throws IOException {
    ensureRemaining(4);
    writeBuffer.putFloat(value);
  }

  @Override
  public void write(double value) throws IOException {
    ensureRemaining(8);
    writeBuffer.putDouble(value);
  }

  @Override
  public void write(byte value1, byte value2) throws IOException {
    ensureRemaining(2);
    writeBuffer.put(value1);
    writeBuffer.put(value2);
  }

  @Override
  public void write(byte value1, short value2) throws IOException {
    ensureRemaining(3);
    writeBuffer.put(value1);
    writeBuffer.putShort(value2);
  }

  @Override
  public void write(byte value1, int value2) throws IOException {
    ensureRemaining(5);
    writeBuffer.put(value1);
    writeBuffer.putInt(value2);
  }

  @Override
  public void write(byte value1, long value2) throws IOException {
    ensureRemaining(9);
    writeBuffer.put(value1);
    writeBuffer.putLong(value2);
  }

  @Override
  public void write(byte value1, float value2) throws IOException {
    ensureRemaining(5);
    writeBuffer.put(value1);
    writeBuffer.putFloat(value2);
  }

  @Override
  public void write(byte value1, double value2) throws IOException {
    ensureRemaining(9);
    writeBuffer.put(value1);
    writeBuffer.putDouble(value2);
  }

  @Override
  public void flushBuffer() throws IOException {
    checkNotClosed();
    doFlushBuffer();
  }

  private void doFlushBuffer() throws IOException {
    writeBuffer.flip();
    provider.write(writeBuffer);
    writeBuffer.clear();
  }
}
