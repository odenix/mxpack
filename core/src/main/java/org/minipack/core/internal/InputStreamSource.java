/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageSource;

/** A message source that reads from an {@link InputStream}. */
public final class InputStreamSource extends MessageSource {
  private final InputStream stream;

  public InputStreamSource(InputStream stream, BufferAllocator allocator) {
    super(allocator);
    this.stream = stream;
  }

  @Override
  protected int doRead(ByteBuffer buffer, int minBytesHint) throws IOException {
    if (!buffer.hasArray()) {
      throw Exceptions.arrayBackedBufferRequired();
    }
    var maxBytesToRead =
        stream.available() >= minBytesHint
            ? Math.min(stream.available(), buffer.remaining())
            : buffer.remaining();
    var bytesRead =
        stream.read(buffer.array(), buffer.arrayOffset() + buffer.position(), maxBytesToRead);
    if (bytesRead > 0) {
      buffer.position(buffer.position() + bytesRead);
    }
    return bytesRead;
  }

  @Override
  protected void doClose() throws IOException {
    stream.close();
  }
}
