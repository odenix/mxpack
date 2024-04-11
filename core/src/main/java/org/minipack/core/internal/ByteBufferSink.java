/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageSink;

public final class ByteBufferSink extends MessageSink {
  private ByteBuffer outputBuffer = ByteBuffer.allocate(0);

  public ByteBufferSink(BufferAllocator allocator) {
    super(allocator);
  }

  public ByteBufferSink(BufferAllocator allocator, int bufferCapacity) {
    super(allocator, bufferCapacity);
  }

  public ByteBuffer outputBuffer() {
    return outputBuffer;
  }

  @Override
  public void doWrite(ByteBuffer buffer) {
    outputBuffer = allocator.ensureRemaining(outputBuffer, buffer.remaining());
    outputBuffer.put(buffer);
  }

  @Override
  public void doWrite(ByteBuffer[] buffers) {
    var remaining = 0;
    for (var buffer : buffers) remaining += buffer.remaining();
    outputBuffer = allocator.ensureRemaining(outputBuffer, remaining);
    for (var buffer : buffers) outputBuffer.put(buffer);
  }

  @Override
  protected void doFlush() {} // nothing to do

  @Override
  protected void doClose() {} // nothing to do
}
