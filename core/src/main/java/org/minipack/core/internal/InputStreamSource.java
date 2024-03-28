/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.minipack.core.MessageSource;

/** A message source that reads from an {@link InputStream}. */
public final class InputStreamSource extends MessageSource {
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

  private final InputStream in;

  public InputStreamSource(InputStream in) {
    super(ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));
    this.in = in;
  }

  public InputStreamSource(InputStream in, ByteBuffer buffer) {
    super(buffer);
    this.in = in;
  }

  @Override
  public int readAny(ByteBuffer buffer, int minBytesHint) throws IOException {
    if (!buffer.hasArray()) {
      throw Exceptions.arrayBackedBufferRequired();
    }
    var maxBytesToRead =
        in.available() >= minBytesHint
            ? Math.min(in.available(), buffer.remaining())
            : buffer.remaining();
    var bytesRead =
        in.read(buffer.array(), buffer.arrayOffset() + buffer.position(), maxBytesToRead);
    buffer.position(buffer.position() + bytesRead);
    return bytesRead;
  }

  @Override
  public void close() throws IOException {
    in.close();
  }
}
