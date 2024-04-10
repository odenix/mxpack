/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageSource;

/** A message source that reads from a blocking {@link ReadableByteChannel}. */
public final class ChannelSource extends MessageSource {
  private final ReadableByteChannel blockingChannel;

  public ChannelSource(ReadableByteChannel blockingChannel, BufferAllocator allocator) {
    super(allocator);
    this.blockingChannel = blockingChannel;
  }

  public ChannelSource(
      ReadableByteChannel blockingChannel, BufferAllocator allocator, int bufferCapacity) {
    super(allocator, bufferCapacity);
    this.blockingChannel = blockingChannel;
  }

  @Override
  protected int doRead(ByteBuffer buffer, int minBytesHint) throws IOException {
    var remaining = buffer.remaining();
    var bytesRead = blockingChannel.read(buffer);
    if (bytesRead == 0 && remaining > 0) {
      throw Exceptions.nonBlockingChannelDetected();
    }
    return bytesRead;
  }

  @Override
  protected void doClose() throws IOException {
    blockingChannel.close();
  }
}
