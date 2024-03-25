/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.translatenix.minipack.internal.ChannelSource;
import org.translatenix.minipack.internal.Exceptions;
import org.translatenix.minipack.internal.InputStreamSource;

/** The underlying source of a {@link MessageReader}. */
public interface MessageSource extends Closeable {
  /**
   * Reads between 1 and {@linkplain ByteBuffer#remaining() remaining} bytes from this source into
   * the given buffer, returning the actual number of bytes read.
   *
   * <p>Returns {@code -1} if no more bytes can be read from this source.
   *
   * <p>{@code minBytesHint} indicates the minimum number of bytes that the caller would like to
   * read. However, unlike {@link #readAtLeast}, this method does not guarantee that more than 1
   * byte will be read.
   */
  int read(ByteBuffer buffer, int minBytesHint) throws IOException;

  /**
   * Reads between {@code minBytes} and {@linkplain ByteBuffer#remaining() remaining} bytes from
   * this source into the given buffer, returning the actual number of bytes read.
   *
   * <p>Throws {@link java.io.EOFException} if the end of input is reached before {@code minBytes}
   * bytes have been read.
   */
  default int readAtLeast(ByteBuffer buffer, int minBytes) throws IOException {
    assert minBytes <= buffer.remaining();
    var totalBytesRead = 0;
    try {
      while (totalBytesRead < minBytes) {
        var bytesRead = read(buffer, minBytes);
        if (bytesRead == -1) {
          throw Exceptions.prematureEndOfInput(minBytes, totalBytesRead);
        }
        totalBytesRead += bytesRead;
      }
    } catch (IOException e) {
      throw Exceptions.ioErrorReadingFromSource(e);
    }
    return totalBytesRead;
  }

  /**
   * Reads enough bytes from this source into the given buffer for {@linkplain ByteBuffer#get()
   * getting} at least {@code length} bytes from the buffer.
   *
   * <p>The number of bytes read is between 0 and {@link ByteBuffer#remaining()}.
   */
  default void ensureRemaining(int length, ByteBuffer buffer) throws IOException {
    int minBytes = length - buffer.remaining();
    if (minBytes > 0) {
      buffer.compact();
      readAtLeast(buffer, minBytes);
      buffer.flip();
      assert buffer.remaining() >= length;
    }
  }

  /** Returns a source that reads from the given input stream. */
  static MessageSource of(InputStream stream) {
    return new InputStreamSource(stream);
  }

  /** Returns a source that reads from the given blocking channel. */
  static MessageSource of(ReadableByteChannel blockingChannel) {
    return new ChannelSource(blockingChannel);
  }
}
