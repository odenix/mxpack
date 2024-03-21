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
  static final class OutputStreamSink implements MessageSink {
    private final OutputStream out;

    OutputStreamSink(OutputStream out) {
      this.out = out;
    }

    @Override
    public void writeBuffer(ByteBuffer buffer) {
      if (!buffer.hasArray()) {
        throw WriterException.arrayBackedBufferRequired();
      }
      try {
        out.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
      } catch (IOException e) {
        throw WriterException.ioError(e);
      }
    }

    @Override
    public void flush() {
      try {
        out.flush();
      } catch (IOException e) {
        throw WriterException.ioError(e);
      }
    }
  }

  static final class ChannelSink implements MessageSink {
    private final WritableByteChannel channel;

    public ChannelSink(WritableByteChannel channel) {
      this.channel = channel;
    }

    @Override
    public void writeBuffer(ByteBuffer buffer) {
      try {
        while (buffer.hasRemaining()) channel.write(buffer);
      } catch (IOException e) {
        throw WriterException.ioError(e);
      }
    }

    @Override
    public void flush() {}
  }
}
