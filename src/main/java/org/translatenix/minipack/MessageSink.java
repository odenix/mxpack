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

/** The underlying sink of a {@link MessageWriter}. */
public interface MessageSink extends Closeable {
  /**
   * Writes the {@linkplain ByteBuffer#remaining() remaining} bytes of the given buffer.
   *
   * <p>Conceptually, this method makes {@code n} calls to {@link ByteBuffer#get()}, where {@code n}
   * is the number of bytes {@linkplain ByteBuffer#remaining() remaining} in the buffer.
   */
  void write(ByteBuffer buffer) throws IOException;

  /** Flushes this message sink. */
  void flush() throws IOException;

  /** Returns a message sink that writes to the given stream. */
  static MessageSink of(OutputStream stream) {
    return new MessageSinks.OutputStreamSink(stream);
  }

  /** Returns a message sink that writes to the given channel. */
  static MessageSink of(WritableByteChannel channel) {
    return new MessageSinks.ChannelSink(channel);
  }
}
