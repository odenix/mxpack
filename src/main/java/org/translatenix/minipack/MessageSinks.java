/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

final class MessageSinks {
  private MessageSinks() {}

  static final class OutputStreamSink implements MessageSink {
    private final OutputStream out;

    OutputStreamSink(OutputStream out) {
      this.out = out;
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
      if (!buffer.hasArray()) {
        throw Exceptions.arrayBackedBufferRequired();
      }
      out.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    @Override
    public void flush() throws IOException {
      out.flush();
    }

    @Override
    public void close() throws IOException {
      out.close();
    }
  }

  static final class ChannelSink implements MessageSink {
    private final WritableByteChannel channel;

    public ChannelSink(WritableByteChannel channel) {
      this.channel = channel;
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
      while (buffer.hasRemaining()) channel.write(buffer);
    }

    @Override
    public void flush() {} // nothing to do

    @Override
    public void close() throws IOException {
      channel.close();
    }
  }
}
