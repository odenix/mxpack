/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.minipack.java.BufferAllocator;
import org.minipack.java.MessageSink;

/** A sink provider that writes to a {@link java.nio.ByteBuffer}. */
public final class BufferSinkProvider implements MessageSink.Provider {
  private final BufferAllocator allocator;
  private final SinkOutput<ByteBuffer> output;
  private ByteBuffer outputBuffer;

  public BufferSinkProvider(SinkOutput<ByteBuffer> output) {
    this(output, options -> {});
  }

  public BufferSinkProvider(SinkOutput<ByteBuffer> output, Consumer<MessageSink.Options> consumer) {
    this.output = output;
    var options = new DefaultMessageSink.DefaultOptions();
    consumer.accept(options);
    this.allocator = options.allocator;
    outputBuffer = allocator.pooledByteBuffer(options.bufferCapacity);
  }

  @Override
  public void write(ByteBuffer buffer) {
    outputBuffer = allocator.ensureRemaining(outputBuffer, buffer.remaining());
    outputBuffer.put(buffer);
  }

  @Override
  public void write(ByteBuffer[] buffers) {
    var remaining = 0;
    for (var buffer : buffers) remaining += buffer.remaining();
    outputBuffer = allocator.ensureRemaining(outputBuffer, remaining);
    for (var buffer : buffers) outputBuffer.put(buffer);
  }

  @Override
  public void flush() {} // nothing to do

  @Override
  public void close() {
    output.set(outputBuffer.flip());
  }
}
