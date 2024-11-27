/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core;

import io.github.odenix.minipack.core.internal.LeasedByteBufferImpl;

import java.nio.ByteBuffer;

/// A [ByteBuffer] leased from a [BufferAllocator].
public sealed interface LeasedByteBuffer extends AutoCloseable permits LeasedByteBufferImpl {
  /// Gets the leased byte buffer.
  ///
  /// **Continued use of a [closed][#close()] byte buffer can result in data corruption**.
  ///
  /// @return the leased byte buffer
  /// @throws IllegalStateException if [#close()] has already been called
  ByteBuffer get();

  /// Returns the leased byte buffer to the buffer allocator it was [obtained][BufferAllocator#getByteBuffer] from.
  ///
  /// Subsequent calls to this method have no effect.
  /// Calling [#get()] after calling this method will throw [IllegalStateException].
  @Override
  void close();
}
