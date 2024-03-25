/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.translatenix.minipack.MessageSink;

/** A message sink that writes to a blocking {@link WritableByteChannel}. */
public final class ChannelSink implements MessageSink {
  private final WritableByteChannel blockingChannel;

  public ChannelSink(WritableByteChannel blockingChannel) {
    this.blockingChannel = blockingChannel;
  }

  @Override
  public int write(ByteBuffer buffer) throws IOException {
    var remaining = buffer.remaining();
    var bytesWritten = blockingChannel.write(buffer);
    if (bytesWritten != remaining) {
      throw Exceptions.nonBlockingWriteableChannel();
    }
    return bytesWritten;
  }

  @Override
  public void flush() {} // nothing to do

  @Override
  public void close() throws IOException {
    blockingChannel.close();
  }
}
