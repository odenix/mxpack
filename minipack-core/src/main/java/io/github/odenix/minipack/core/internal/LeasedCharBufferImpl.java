/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.internal;

import org.jspecify.annotations.Nullable;
import io.github.odenix.minipack.core.LeasedCharBuffer;

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
