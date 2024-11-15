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
public final class BufferSinkProvider implements MessageSink.Provider<ByteBuffer> {
  private final BufferAllocator allocator;
  private ByteBuffer output;

  public BufferSinkProvider() {
    this(options -> {});
  }

  public BufferSinkProvider(Consumer<MessageSink.Options> consumer) {
    var options = new DefaultMessageSink.DefaultOptions();
    consumer.accept(options);
    this.allocator = options.allocator;
    output = allocator.acquireByteBuffer(0); // TODO: initial size
  }

  @Override
  public void write(ByteBuffer buffer) {
    output = allocator.ensureRemaining(output, buffer.remaining());
    output.put(buffer);
  }

  @Override
  public void write(ByteBuffer[] buffers) {
    var remaining = 0;
    for (var buffer : buffers) remaining += buffer.remaining();
    output = allocator.ensureRemaining(output, remaining);
    for (var buffer : buffers) output.put(buffer);
  }

  @Override
  public void flush() {} // nothing to do

  @Override
  public void close() {} // nothing to do

  @Override
  public ByteBuffer output() {
    return output;
  }
}
