/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import org.jspecify.annotations.Nullable;
import org.minipack.core.LeasedByteBuffer;

import java.nio.ByteBuffer;

/// Implementation of [LeasedByteBuffer].
public final class LeasedByteBufferImpl extends AbstractCloseable implements LeasedByteBuffer {
  final ByteBuffer buffer;
  private final @Nullable PooledBufferAllocator pool;

  public LeasedByteBufferImpl(ByteBuffer buffer, @Nullable PooledBufferAllocator pool) {
    this.buffer = buffer;
    this.pool = pool;
  }

  @Override
  public ByteBuffer get() {
    checkNotClosed();
    return buffer;
  }

  @Override
  public void close() {
    if (getAndSetClosed()) return;
    if (pool != null) pool.release(this);
  }
}
