/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.translatenix.minipack.MessageSource;

/** A message source that reads from a {@link ReadableByteChannel}. */
public final class ChannelSource implements MessageSource {
  private final ReadableByteChannel channel;

  public ChannelSource(ReadableByteChannel channel) {
    this.channel = channel;
  }

  @Override
  public int read(ByteBuffer buffer, int minBytesHint) throws IOException {
    return channel.read(buffer);
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }
}
