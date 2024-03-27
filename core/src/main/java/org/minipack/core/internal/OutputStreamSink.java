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
  private final OutputStream out;

  public OutputStreamSink(OutputStream out) {
    this.out = out;
  }

  @Override
  public int write(ByteBuffer buffer) throws IOException {
    if (!buffer.hasArray()) {
      throw Exceptions.arrayBackedBufferRequired();
    }
    buffer.flip();
    var bytesToWrite = buffer.limit();
    out.write(buffer.array(), buffer.arrayOffset(), bytesToWrite);
    buffer.clear();
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
