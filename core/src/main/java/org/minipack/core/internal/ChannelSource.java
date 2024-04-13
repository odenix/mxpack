/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
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
  protected void doSkip(int length) throws IOException {
    if (length == 0) return;
    if (blockingChannel instanceof SeekableByteChannel channel) {
      channel.position(channel.position() + length);
      return;
    }
    var capacity = buffer.capacity();
    var bytesToSkip = length;
    while (bytesToSkip > 0) {
      buffer.position(0).limit(Math.min(bytesToSkip, capacity));
      bytesToSkip -= doRead(buffer, bytesToSkip);
    }
  }

  @Override
  protected void doClose() throws IOException {
    blockingChannel.close();
  }

  @Override
  public long transferTo(WritableByteChannel channel, long maxBytesToTransfer) throws IOException {
    if (blockingChannel instanceof FileChannel fileChannel) {
      var bytesWritten = writeBufferTo(channel, maxBytesToTransfer);
      var bytesTransferred =
          fileChannel.transferTo(
              fileChannel.position(), maxBytesToTransfer - bytesWritten, channel);
      fileChannel.position(fileChannel.position() + bytesTransferred);
      return bytesWritten + bytesTransferred;
    }
    if (channel instanceof FileChannel fileChannel) {
      var bytesWritten = writeBufferTo(channel, maxBytesToTransfer);
      var bytesTransferred =
          fileChannel.transferFrom(
              blockingChannel, fileChannel.position(), maxBytesToTransfer - bytesWritten);
      return bytesWritten + bytesTransferred;
    }
    return super.transferTo(channel, maxBytesToTransfer);
  }

  private int writeBufferTo(WritableByteChannel channel, long maxBytesToTransfer)
      throws IOException {
    var bytesToWrite = (int) Math.min(maxBytesToTransfer, buffer.remaining());
    var savedLimit = buffer.limit();
    buffer.limit(buffer.position() + bytesToWrite);
    var bytesWritten = channel.write(buffer);
    buffer.limit(savedLimit);
    if (bytesWritten != bytesToWrite) {
      throw Exceptions.nonBlockingChannelDetected();
    }
    return bytesWritten;
  }
}
