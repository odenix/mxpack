/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.minipack.core.MessageSink;

/** A message sink that writes to an {@link OutputStream}. */
public final class OutputStreamSink extends MessageSink {
  private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

  private final OutputStream out;

  public OutputStreamSink(OutputStream out) {
    super(ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));
    this.out = out;
  }

  public OutputStreamSink(OutputStream out, ByteBuffer buffer) {
    super(buffer);
    this.out = out;
  }

  @Override
  public int write(ByteBuffer buffer) throws IOException {
    if (!buffer.hasArray()) {
      throw Exceptions.arrayBackedBufferRequired();
    }
    var bytesToWrite = buffer.remaining();
    out.write(buffer.array(), buffer.arrayOffset() + buffer.position(), bytesToWrite);
    buffer.position(buffer.position() + bytesToWrite);
    return bytesToWrite;
  }

  @Override
  public void flush() throws IOException {
    out.flush();
  }

  @Override
  public void close() throws IOException {
    out.close();
  }
}
