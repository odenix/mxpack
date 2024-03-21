/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.nio.ByteBuffer;

public interface MessageSink {
  /**
   * Writes the {@linkplain ByteBuffer#remaining() remaining} bytes of the given buffer.
   *
   * <p>Throws {@link WriterException} if an error occurs during writing.
   */
  // Any benefit in changing this to writeBuffer(buffer, minBytes)?
  void writeBuffer(ByteBuffer buffer);

  /**
   * Flushes this sink.
   *
   * <p>Throws {@link WriterException} if an error occurs during flushing.
   */
  void flush();
}
