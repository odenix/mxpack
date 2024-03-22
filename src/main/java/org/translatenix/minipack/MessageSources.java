/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

final class MessageSources {
  static final class InputStreamSource implements MessageSource {
    private final InputStream in;

    public InputStreamSource(InputStream in) {
      this.in = in;
    }

    @Override
    public void read(ByteBuffer buffer, int minBytes) throws IOException {
      assert minBytes > 0;
      assert minBytes <= buffer.remaining();
      if (!buffer.hasArray()) throw MessageSource.arrayBackedBufferRequired();

      var totalBytesRead = 0;
      while (totalBytesRead < minBytes) {
        var bytesToRead =
            in.available() >= (minBytes - totalBytesRead)
                ? Math.min(in.available(), buffer.remaining())
                : buffer.remaining();
        var bytesRead =
            in.read(buffer.array(), buffer.arrayOffset() + buffer.position(), bytesToRead);
        if (bytesRead == -1) {
          throw MessageSource.prematureEndOfInput(minBytes, totalBytesRead);
        }
        assert bytesRead <= bytesToRead;
        buffer.position(buffer.position() + bytesRead);
        totalBytesRead += bytesRead;
      }
    }
  }

  static final class Channel implements MessageSource {
    private final ReadableByteChannel channel;

    public Channel(ReadableByteChannel channel) {
      this.channel = channel;
    }

    @Override
    public void read(ByteBuffer buffer, int minBytes) throws IOException {
      var totalBytesRead = 0;
      while (totalBytesRead < minBytes) {
        var bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
          throw MessageSource.prematureEndOfInput(minBytes, totalBytesRead);
        }
        totalBytesRead += bytesRead;
      }
    }
  }
}
