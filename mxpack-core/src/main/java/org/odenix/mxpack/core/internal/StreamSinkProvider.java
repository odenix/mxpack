/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.odenix.mxpack.core.MessageSink;

/// A sink provider that writes to an [OutputStream].
public final class StreamSinkProvider implements MessageSink.Provider {
  private final OutputStream stream;

  public StreamSinkProvider(OutputStream stream) {
    this.stream = stream;
  }

  @Override
  public void write(ByteBuffer buffer) throws IOException {
    if (!buffer.hasArray()) {
      throw Exceptions.arrayBackedBufferRequired();
    }
    stream.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
  }

  @Override
  public void write(ByteBuffer[] buffers) throws IOException {
    for (var buffer : buffers) write(buffer);
  }

  @Override
  public void flush() throws IOException {
    stream.flush();
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }
}
