/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.nio.ByteBuffer;

public interface MessageSource {
  /**
   * Reads at least {@code minBytes} bytes into the given buffer.
   *
   * <p>Conceptually, this method makes {@code n} calls to {@link ByteBuffer#put(byte)}, where
   * {@code n} is between {@code minBytes} and the buffer's {@linkplain ByteBuffer#remaining()
   * remaining} bytes.
   *
   * <p>Throws {@link ReaderException} if an error occurs during reading.
   */
  void read(ByteBuffer buffer, int minBytes);
}
