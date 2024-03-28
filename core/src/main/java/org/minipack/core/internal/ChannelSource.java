/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.minipack.core.MessageSource;

/** A message source that reads from a blocking {@link ReadableByteChannel}. */
public final class ChannelSource extends MessageSource {
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

  private final ReadableByteChannel blockingChannel;

  public ChannelSource(ReadableByteChannel blockingChannel) {
    super(ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));
    this.blockingChannel = blockingChannel;
  }

  public ChannelSource(ReadableByteChannel blockingChannel, ByteBuffer buffer) {
    super(buffer);
    this.blockingChannel = blockingChannel;
  }

  @Override
  public int readAny(ByteBuffer buffer, int minBytesHint) throws IOException {
    var remaining = buffer.remaining();
    var bytesRead = blockingChannel.read(buffer);
    if (bytesRead == 0 && remaining > 0) {
      throw Exceptions.nonBlockingReadableChannel();
    }
    return bytesRead;
  }

  @Override
  public void close() throws IOException {
    blockingChannel.close();
  }
}
