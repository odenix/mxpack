/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.minipack.java.MessageSink;

/** A source provider that writes to a blocking {@link WritableByteChannel}. */
public final class ChannelSinkProvider implements MessageSink.Provider<Void> {
  private final WritableByteChannel blockingChannel;

  public ChannelSinkProvider(WritableByteChannel blockingChannel) {
    this.blockingChannel = blockingChannel;
  }

  @Override
  public void write(ByteBuffer buffer) throws IOException {
    var remaining = buffer.remaining();
    var bytesWritten = blockingChannel.write(buffer);
    if (bytesWritten != remaining) {
      throw Exceptions.nonBlockingChannelDetected();
    }
  }

  @Override
  public void write(ByteBuffer[] buffers) throws IOException {
    if (blockingChannel instanceof GatheringByteChannel channel) {
      channel.write(buffers);
      return;
    }
    for (var buffer : buffers) write(buffer);
  }

  @Override
  public long transferFrom(ReadableByteChannel channel, long length, ByteBuffer buffer)
      throws IOException {
    if (blockingChannel instanceof FileChannel fileChannel) {
      var bytesTransferred = fileChannel.transferFrom(channel, fileChannel.position(), length);
      fileChannel.position(fileChannel.position() + bytesTransferred);
      return bytesTransferred;
    }
    if (channel instanceof FileChannel fileChannel) {
      return fileChannel.transferTo(fileChannel.position(), length, blockingChannel);
    }
    return MessageSink.Provider.super.transferFrom(channel, length, buffer);
  }

  @Override
  public void flush() {} // nothing to do

  @Override
  public void close() throws IOException {
    blockingChannel.close();
  }
}
