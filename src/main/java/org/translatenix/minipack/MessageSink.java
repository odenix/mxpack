/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.translatenix.minipack.internal.ChannelSink;
import org.translatenix.minipack.internal.OutputStreamSink;

/** The underlying sink of a {@link MessageWriter}. */
public interface MessageSink extends Closeable {
  /**
   * Writes between 1 and {@linkplain ByteBuffer#remaining() remaining} bytes from the given buffer
   * to this sink, returning the actual number of bytes written.
   */
  int write(ByteBuffer buffer) throws IOException;

  /** Flushes this sink. */
  void flush() throws IOException;

  /**
   * Writes enough bytes from the given buffer to this sink for {@linkplain ByteBuffer#put putting}
   * at least {@code byteCount} bytes into the buffer.
   *
   * <p>The number of bytes written is between 0 and {@linkplain ByteBuffer#remaining() remaining}.
   */
  default void ensureRemaining(ByteBuffer buffer, int byteCount) throws IOException {
    assert byteCount <= buffer.capacity();
    var minBytes = byteCount - buffer.remaining();
    if (minBytes > 0) {
      buffer.flip();
      write(buffer);
      buffer.compact();
    }
  }

  /** Returns a sink that writes to the given output stream. */
  static MessageSink of(OutputStream stream) {
    return new OutputStreamSink(stream);
  }

  /** Returns a sink that writes to the given blocking channel. */
  static MessageSink of(WritableByteChannel blockingChannel) {
    return new ChannelSink(blockingChannel);
  }
}
