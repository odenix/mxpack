/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import org.odenix.mxpack.core.internal.LeasedByteBufferImpl;

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
