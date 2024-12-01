/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

import java.nio.ByteBuffer;

import org.odenix.mxpack.core.*;

/// A sink provider that writes to a [java.nio.ByteBuffer].
public final class BufferSinkProvider extends AbstractCloseable implements MessageSink.Provider {
  private final BufferAllocator allocator;
  private final MessageOutputImpl.BufferImpl output;
  private LeasedByteBuffer outputBuffer;

  public BufferSinkProvider(MessageOutput.Buffer output, BufferAllocator allocator) {
    this.output = (MessageOutputImpl.BufferImpl) output;
    this.allocator = allocator;
    outputBuffer = allocator.getByteBuffer(this.output.initialCapacity);
  }

  @Override
  public void write(ByteBuffer buffer) {
    checkNotClosed();
    growOutputBuffer(buffer.remaining()).put(buffer);
  }

  @Override
  public void write(ByteBuffer[] buffers) {
    checkNotClosed();
    long remaining = 0;
    for (var buffer : buffers) {
      remaining += buffer.remaining();
    }
    var buf = growOutputBuffer(remaining);
    for (var buffer : buffers) {
      buf.put(buffer);
    }
  }

  private ByteBuffer growOutputBuffer(long remaining) {
    var buffer = outputBuffer.get();
    if (buffer.remaining() >= remaining) {
      return buffer;
    }
    var minCapacity = buffer.position() + remaining;
    var maxCapacity = allocator.maxByteBufferCapacity();
    if (minCapacity > maxCapacity) {
      throw Exceptions.bufferSizeLimitExceeded(minCapacity, maxCapacity);
    }
    var preferredCapacity = buffer.capacity() * 2L;
    var newCapacity = Math.min(maxCapacity, Math.max(preferredCapacity, minCapacity));
    var newOutputBuffer = allocator.getByteBuffer((int) newCapacity);
    var newBuffer = newOutputBuffer.get();
    newBuffer.put(buffer);
    outputBuffer.close();
    outputBuffer = newOutputBuffer;
    return newBuffer;
  }

  @Override
  public void flush() {
    checkNotClosed();
  }

  @Override
  public void close() {
    if (getAndSetClosed()) return;
    outputBuffer.get().flip();
    output.set(outputBuffer);
  }
}
