/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

import org.jspecify.annotations.Nullable;
import org.odenix.mxpack.core.LeasedCharBuffer;

import java.nio.CharBuffer;

/// Implementation of [LeasedCharBuffer].
public final class LeasedCharBufferImpl extends AbstractCloseable implements LeasedCharBuffer {
  final CharBuffer buffer;
  private final @Nullable PooledBufferAllocator pool;

  public LeasedCharBufferImpl(CharBuffer buffer, @Nullable PooledBufferAllocator pool) {
    this.buffer = buffer;
    this.pool = pool;
  }

  @Override
  public CharBuffer get() {
    checkNotClosed();
    return buffer;
  }

  @Override
  public void close() {
    if (getAndSetClosed()) return;
    if (pool != null) pool.release(this);
  }
}
