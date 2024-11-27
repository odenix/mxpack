/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import org.minipack.core.internal.LeasedCharBufferImpl;

import java.nio.CharBuffer;

/// A [CharBuffer] leased from a [BufferAllocator].
public sealed interface LeasedCharBuffer extends AutoCloseable permits LeasedCharBufferImpl {
  /// Gets the leased char buffer.
  ///
  /// **Continued use of a [closed][#close()] char buffer can result in data corruption**.
  ///
  /// @return the leased char buffer
  /// @throws IllegalStateException if [#close()] has already been called
  CharBuffer get();

  /// Returns the leased char buffer to the buffer allocator it was [obtained][BufferAllocator#getCharBuffer] from.
  ///
  /// Subsequent calls to this method have no effect.
  /// Calling [#get()] after calling this method will throw [IllegalStateException].
  @Override
  void close();
}
