/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.nio.ByteBuffer;

/**
 * Allocates {@linkplain ByteBuffer byte buffers} on demand.
 */
public interface BufferAllocator {
  /**
   * Returns a {@code ByteBuffer} that is backed by an accessible array and has at least the given
   * capacity.
   *
   * <p>Ownership of the returned buffer is transferred to the caller until the next invocation of
   * this method.
   */
  ByteBuffer getArrayBackedBuffer(int capacity);
}
