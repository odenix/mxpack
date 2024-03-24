/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.translatenix.minipack.MessageSink;

/** A message sink that writes to an {@link OutputStream}. */
public final class OutputStreamSink implements MessageSink {
  private final OutputStream out;

  public OutputStreamSink(OutputStream out) {
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
