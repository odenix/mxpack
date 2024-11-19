/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.minipack.java.BufferAllocator;
import org.minipack.java.MessageSink;

/// A sink provider that writes to a [java.nio.ByteBuffer].
public final class BufferSinkProvider implements MessageSink.Provider {
  private final BufferAllocator allocator;
  private final SinkOutput<BufferAllocator.PooledByteBuffer> output;
  private BufferAllocator.PooledByteBuffer outputBuffer;

  public BufferSinkProvider(SinkOutput<BufferAllocator.PooledByteBuffer> output) {
    this(output, options -> {});
  }

  public BufferSinkProvider(
      SinkOutput<BufferAllocator.PooledByteBuffer> output, Consumer<MessageSink.Options> consumer) {
    this.output = output;
    var options = new DefaultMessageSink.DefaultOptions();
    consumer.accept(options);
    this.allocator = options.allocator;
    outputBuffer = allocator.getByteBuffer(options.bufferCapacity);
  }

  @Override
  public void write(ByteBuffer buffer) {
    outputBuffer = allocator.ensureRemaining(outputBuffer, buffer.remaining());
    outputBuffer.value().put(buffer);
  }

  @Override
  public void write(ByteBuffer[] buffers) {
    var remaining = 0;
    for (var buffer : buffers) remaining += buffer.remaining();
    outputBuffer = allocator.ensureRemaining(outputBuffer, remaining);
    for (var buffer : buffers) outputBuffer.value().put(buffer);
  }

  @Override
  public void flush() {} // nothing to do

  @Override
  public void close() {
    outputBuffer.value().flip();
    output.set(outputBuffer);
  }
}
