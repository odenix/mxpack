/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.minipack.java.MessageSource;

/** A source provider that reads from a blocking {@link ReadableByteChannel}. */
public final class ChannelSourceProvider implements MessageSource.Provider {
  private final ReadableByteChannel blockingChannel;

  public ChannelSourceProvider(ReadableByteChannel blockingChannel) {
    this.blockingChannel = blockingChannel;
  }

  @Override
  public int read(ByteBuffer buffer, int minBytesHint) throws IOException {
    var remaining = buffer.remaining();
    var bytesRead = blockingChannel.read(buffer);
    if (bytesRead == 0 && remaining > 0) {
      throw Exceptions.nonBlockingChannelDetected();
    }
    return bytesRead;
  }

  @Override
  public void skip(int length) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(0); // TODO: where to get this temp buffer from?
    if (length == 0) return;
    if (blockingChannel instanceof SeekableByteChannel channel) {
      channel.position(channel.position() + length);
      return;
    }
    var capacity = buffer.capacity();
    var bytesToSkip = length;
    while (bytesToSkip > 0) {
      buffer.position(0).limit(Math.min(bytesToSkip, capacity));
      bytesToSkip -= read(buffer, bytesToSkip);
    }
  }

  @Override
  public void close() throws IOException {
    blockingChannel.close();
  }

  @Override
  public long transferTo(WritableByteChannel channel, long maxBytesToTransfer) throws IOException {
    if (blockingChannel instanceof FileChannel fileChannel) {
      var bytesTransferred =
          fileChannel.transferTo(fileChannel.position(), maxBytesToTransfer, channel);
      fileChannel.position(fileChannel.position() + bytesTransferred);
      return bytesTransferred;
    }
    if (channel instanceof FileChannel fileChannel) {
      return fileChannel.transferFrom(blockingChannel, fileChannel.position(), maxBytesToTransfer);
    }
    return MessageSource.Provider.super.transferTo(channel, maxBytesToTransfer);
  }
}
