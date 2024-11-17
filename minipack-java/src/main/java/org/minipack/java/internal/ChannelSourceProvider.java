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
  private final ReadableByteChannel sourceChannel;

  public ChannelSourceProvider(ReadableByteChannel sourceChannel) {
    this.sourceChannel = sourceChannel;
  }

  @Override
  public int read(ByteBuffer buffer, int minBytesHint) throws IOException {
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
    var remaining = buffer.remaining();
    if (length > remaining && sourceChannel instanceof SeekableByteChannel seekableChannel) {
      buffer.clear();
      seekableChannel.position(seekableChannel.position() + (length - remaining));
      return;
    }
    MessageSource.Provider.super.skip(length, buffer);
  }

  @Override
  public long transferTo(WritableByteChannel destination, long length, ByteBuffer buffer)
      throws IOException {
    var remaining = buffer.remaining();
    if (length > remaining && sourceChannel instanceof FileChannel fileChannel) {
      var bytesWritten = destination.write(buffer);
      if (bytesWritten != length) {
        throw Exceptions.nonBlockingChannelDetected();
      }
      var bytesTransferred =
          fileChannel.transferTo(fileChannel.position(), length - remaining, destination);
      fileChannel.position(fileChannel.position() + bytesTransferred);
      return bytesTransferred;
    }
    if (length > remaining && destination instanceof FileChannel fileChannel) {
      var bytesWritten = destination.write(buffer);
      if (bytesWritten != length) {
        throw Exceptions.nonBlockingChannelDetected();
      }
      return fileChannel.transferFrom(sourceChannel, fileChannel.position(), length - remaining);
    }
    return MessageSource.Provider.super.transferTo(destination, length, buffer);
  }
}
