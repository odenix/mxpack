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
import org.translatenix.minipack.internal.MessageSources;

/** The underlying source of a {@link MessageReader}. */
public interface MessageSource extends Closeable {
  /**
   * Reads at least {@code minBytes} bytes into the given buffer.
   *
   * <p>Conceptually, this method makes {@code n} calls to {@link ByteBuffer#put(byte)}, where
   * {@code n} is between {@code minBytes} and the buffer's {@linkplain ByteBuffer#remaining()
   * remaining} bytes.
   */
  int read(ByteBuffer buffer, int minBytesHint) throws IOException;

  /** Returns a message source that reads from the given stream. */
  static MessageSource of(InputStream stream) {
    return new MessageSources.InputStreamSource(stream);
  }

  /** Returns a message source that reads from the given channel. */
  static MessageSource of(ReadableByteChannel channel) {
    return new MessageSources.Channel(channel);
  }
}
