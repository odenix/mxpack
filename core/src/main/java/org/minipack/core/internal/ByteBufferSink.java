/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageSink;

/**
 * Note: By design, minipack is optimized for "real" sinks such as OutputStreamSink and especially
 * ChannelSink. Writing to an in-memory sink such as ByteBufferSink will result in some buffer
 * copying:
 *
 * <ul>
 *   <li>Every byte written to ByteBufferSink is eventually copied to outputBuffer() (in batches).
 *   <li>If outputBuffer() needs to grow, its content is copied to the new output buffer.
 * </ul>
 *
 * For most usages, this buffer copying should not present a problem.
 */
public final class ByteBufferSink extends MessageSink {
  private ByteBuffer outputBuffer = ByteBuffer.allocate(0);

  public ByteBufferSink(BufferAllocator allocator) {
    super(allocator);
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
