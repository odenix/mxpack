/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.translatenix.minipack.MessageSink;

/** A message sink that writes to a {@link WritableByteChannel}. */
public final class ChannelSink implements MessageSink {
  private final WritableByteChannel channel;

  public ChannelSink(WritableByteChannel channel) {
    this.channel = channel;
  }

  @Override
  public int write(ByteBuffer buffer) throws IOException {
    return channel.write(buffer);
  }

  @Override
  public void flush() {} // nothing to do

  @Override
  public void close() throws IOException {
    channel.close();
  }
}
