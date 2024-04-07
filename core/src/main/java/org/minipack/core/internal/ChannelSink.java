/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageSink;

/** A message sink that writes to a blocking {@link WritableByteChannel}. */
public final class ChannelSink extends MessageSink {
  private final WritableByteChannel blockingChannel;

  public ChannelSink(WritableByteChannel blockingChannel, BufferAllocator allocator) {
    super(allocator);
    this.blockingChannel = blockingChannel;
  }

  @Override
  public long transferFrom(ReadableByteChannel channel, long maxBytesToTransfer)
      throws IOException {
    if (blockingChannel instanceof FileChannel fileChannel) {
      return fileChannel.transferFrom(channel, fileChannel.position(), maxBytesToTransfer);
    }
    if (channel instanceof FileChannel fileChannel) {
      return fileChannel.transferTo(fileChannel.position(), maxBytesToTransfer, blockingChannel);
    }
    return super.transferFrom(channel, maxBytesToTransfer);
  }

  @Override
  protected void doWrite(ByteBuffer buffer) throws IOException {
    var remaining = buffer.remaining();
    var bytesWritten = blockingChannel.write(buffer);
    if (bytesWritten != remaining) {
      throw Exceptions.nonBlockingChannelDetected();
    }
  }

  @Override
  protected void doWrite(ByteBuffer[] buffers) throws IOException {
    if (blockingChannel instanceof GatheringByteChannel channel) {
      channel.write(buffers);
      return;
    }
    for (var buffer : buffers) doWrite(buffer);
  }

  @Override
  protected void doFlush() {} // nothing to do

  @Override
  protected void doClose() throws IOException {
    blockingChannel.close();
  }
}
