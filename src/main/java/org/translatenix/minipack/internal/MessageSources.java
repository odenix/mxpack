/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.translatenix.minipack.MessageSource;

public final class MessageSources {
  private MessageSources() {}

  public static final class InputStreamSource implements MessageSource {
    private final InputStream in;

    public InputStreamSource(InputStream in) {
      this.in = in;
    }

    @Override
    public int read(ByteBuffer buffer, int minBytesHint) throws IOException {
      if (!buffer.hasArray()) {
        throw Exceptions.arrayBackedBufferRequired();
      }
      var maxBytesToRead =
          in.available() >= minBytesHint
              ? Math.min(in.available(), buffer.remaining())
              : buffer.remaining();
      var bytesRead =
          in.read(buffer.array(), buffer.arrayOffset() + buffer.position(), maxBytesToRead);
      buffer.position(buffer.position() + bytesRead);
      return bytesRead;
    }

    @Override
    public void close() throws IOException {
      in.close();
    }
  }

  public static final class Channel implements MessageSource {
    private final ReadableByteChannel channel;

    public Channel(ReadableByteChannel channel) {
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
}
