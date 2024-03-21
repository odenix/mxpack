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
    public void read(ByteBuffer buffer, int minBytes) {
      assert minBytes > 0;
      assert minBytes <= buffer.remaining();
      if (!buffer.hasArray()) throw ReaderException.arrayBackedBufferRequired();

      var minMoreBytesToRead = minBytes;
      try {
        while (minMoreBytesToRead > 0) {
          var optimalBytesToRead =
              in.available() >= minMoreBytesToRead
                  ? Math.min(in.available(), buffer.remaining())
                  : buffer.remaining();
          var bytesRead =
              in.read(buffer.array(), buffer.arrayOffset() + buffer.position(), optimalBytesToRead);
          if (bytesRead == -1) {
            throw ReaderException.prematureEndOfInput(minBytes, minBytes - minMoreBytesToRead);
          }
          assert bytesRead <= optimalBytesToRead;
          buffer.position(buffer.position() + bytesRead);
          minMoreBytesToRead -= bytesRead;
        }
      } catch (IOException e) {
        throw ReaderException.ioError(e);
      }
    }
  }

  static final class Channel implements MessageSource {
    private final ReadableByteChannel channel;

    public Channel(ReadableByteChannel channel) {
      this.channel = channel;
    }

    @Override
    public void read(ByteBuffer buffer, int minBytes) {
      var totalBytesRead = 0;
      try {
        while (totalBytesRead < minBytes) {
          var bytesRead = channel.read(buffer);
          if (bytesRead == -1) {
            throw ReaderException.prematureEndOfInput(minBytes, totalBytesRead);
          }
          totalBytesRead += bytesRead;
        }
      } catch (IOException e) {
        throw ReaderException.ioError(e);
      }
    }
  }
}
