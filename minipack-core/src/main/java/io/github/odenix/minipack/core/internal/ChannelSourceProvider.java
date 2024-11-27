/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import io.github.odenix.minipack.core.MessageSource;

/// A source provider that reads from a blocking [ReadableByteChannel].
public final class ChannelSourceProvider implements MessageSource.Provider {
  private final ReadableByteChannel sourceChannel;

  public ChannelSourceProvider(ReadableByteChannel sourceChannel) {
    this.sourceChannel = sourceChannel;
  }

  @Override
  public int read(ByteBuffer buffer, int minLengthHint) throws IOException {
    var remaining = buffer.remaining();
    var bytesRead = sourceChannel.read(buffer);
    if (bytesRead == 0 && remaining > 0) {
      throw Exceptions.nonBlockingChannelDetected();
    }
    return bytesRead;
  }

  @Override
  public void close() throws IOException {
    sourceChannel.close();
  }

  @Override
  public void skip(int length, ByteBuffer buffer) throws IOException {
    if (length == 0) return;
    var remaining = buffer.remaining();
    if (length > remaining && sourceChannel instanceof SeekableByteChannel seekableChannel) {
      buffer.position(buffer.limit());
      seekableChannel.position(seekableChannel.position() + (length - remaining));
      return;
    }
    MessageSource.Provider.super.skip(length, buffer);
  }

  @Override
  public long transferTo(WritableByteChannel channel, long length, ByteBuffer buffer)
      throws IOException {
    if (length == 0) return 0;
    var remaining = buffer.remaining();
    if (length > remaining && sourceChannel instanceof FileChannel fileChannel) {
      var bytesWritten = channel.write(buffer);
      if (bytesWritten != remaining) {
        throw Exceptions.nonBlockingChannelDetected();
      }
      var bytesTransferred =
          fileChannel.transferTo(fileChannel.position(), length - remaining, channel);
      fileChannel.position(fileChannel.position() + bytesTransferred);
      return bytesTransferred;
    }
    if (length > remaining && channel instanceof FileChannel fileChannel) {
      var bytesWritten = channel.write(buffer);
      if (bytesWritten != remaining) {
        throw Exceptions.nonBlockingChannelDetected();
      }
      return fileChannel.transferFrom(sourceChannel, fileChannel.position(), length - remaining);
    }
    return MessageSource.Provider.super.transferTo(channel, length, buffer);
  }
}
