/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.nio.ByteBuffer;

/** Allocates {@linkplain ByteBuffer byte buffers} on demand. */
public interface BufferAllocator {
  /**
   * Returns a {@code ByteBuffer} that is backed by an accessible array and has at least the given
   * capacity.
   *
   * <p>Ownership of the returned buffer is transferred to the caller until the next invocation of
   * this method.
   */
  ByteBuffer getArrayBackedBuffer(int capacity);

  /**
   * Returns a buffer allocator that initially returns a buffer with a capacity no less than {@code
   * minCapacity}, growing it as needed until reaching {@code maxCapacity}.
   *
   * <p>If a buffer capacity greater than {@code maxCapacity} is requested, the allocator throws
   * {@link Exceptions#maxCapacityExceeded}.
   */
  static BufferAllocator withCapacity(int minCapacity, int maxCapacity) {
    return new BufferAllocators.DefaultAllocator(minCapacity, maxCapacity);
  }
}
