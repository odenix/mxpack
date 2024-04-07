/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageSink;

/** A message sink that writes to an {@link OutputStream}. */
public final class OutputStreamSink extends MessageSink {
  private final OutputStream stream;

  public OutputStreamSink(OutputStream stream, BufferAllocator allocator) {
    super(allocator);
    this.stream = stream;
  }

  @Override
  protected void doWrite(ByteBuffer buffer) throws IOException {
    if (!buffer.hasArray()) {
      throw Exceptions.arrayBackedBufferRequired();
    }
    stream.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
  }

  @Override
  protected void doWrite(ByteBuffer[] buffers) throws IOException {
    for (var buffer : buffers) doWrite(buffer);
  }

  @Override
  protected void doFlush() throws IOException {
    stream.flush();
  }

  @Override
  protected void doClose() throws IOException {
    stream.close();
  }
}
