/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface MessageSink {
  /** Writes the {@linkplain ByteBuffer#remaining() remaining} bytes of the given buffer. */
  // Any benefit in changing this to writeBuffer(buffer, minBytes)?
  void writeBuffer(ByteBuffer buffer) throws IOException;

  /** Flushes this sink. */
  void flush() throws IOException;

  static IllegalArgumentException arrayBackedBufferRequired() {
    return new IllegalArgumentException(
        "This message sink requires a ByteBuffer backed by an accessible array"
            + " (buffer.hasArray()).");
  }
}
