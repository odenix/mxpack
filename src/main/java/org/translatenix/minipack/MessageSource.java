/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.EOFException;
import java.io.IOException;
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
  void read(ByteBuffer buffer, int minBytes) throws IOException;

  static IllegalArgumentException arrayBackedBufferRequired() {
    return new IllegalArgumentException(
        "This message source requires a ByteBuffer backed by an accessible array"
            + " (buffer.hasArray()).");
  }

  static EOFException prematureEndOfInput(int minBytes, int bytesRead) {
    return new EOFException(
        "Expected at least "
            + minBytes
            + " more bytes, but reached end of input after "
            + bytesRead
            + " bytes.");
  }
}
