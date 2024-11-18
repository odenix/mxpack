/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;
import org.minipack.java.BufferAllocator;

public abstract class AbstractBufferAllocator implements BufferAllocator {
  protected final int maxCapacity;
  protected final boolean preferDirect;

  protected AbstractBufferAllocator(int maxCapacity, boolean preferDirect) {
    this.maxCapacity = maxCapacity;
    this.preferDirect = preferDirect;
  }

  public static final class DefaultPooledByteBuffer implements PooledByteBuffer {
    final ByteBuffer value;
    private final @Nullable PooledBufferAllocator pool;
    private final AtomicBoolean isClosed = new AtomicBoolean();

    public DefaultPooledByteBuffer(ByteBuffer value, @Nullable PooledBufferAllocator pool) {
      this.value = value;
      this.pool = pool;
    }

    @Override
    public ByteBuffer value() {
      if (isClosed.get()) {
        throw Exceptions.pooledBufferAlreadyClosed();
      }
      return value;
    }

    @Override
    public void close() {
      if (!isClosed.getAndSet(true)) {
        if (pool != null) pool.release(this);
      }
    }
  }

  public static final class DefaultPooledCharBuffer implements PooledCharBuffer {
    final CharBuffer value;
    private final @Nullable PooledBufferAllocator pool;
    private final AtomicBoolean isClosed = new AtomicBoolean();

    public DefaultPooledCharBuffer(CharBuffer value, @Nullable PooledBufferAllocator pool) {
      this.value = value;
      this.pool = pool;
    }

    @Override
    public CharBuffer value() {
      if (isClosed.get()) {
        throw Exceptions.pooledBufferAlreadyClosed();
      }
      return value;
    }

    @Override
    public void close() {
      if (!isClosed.getAndSet(true)) {
        if (pool != null) pool.release(this);
      }
    }
  }

  @Override
  public PooledByteBuffer ensureRemaining(PooledByteBuffer pooled, long remaining) {
    var buffer = pooled.value();
    if (buffer.remaining() >= remaining) return pooled;
    pooled.close();
    var minCapacity = checkCapacity(buffer.position() + remaining);
    var growthCapacity = Math.min(maxCapacity, buffer.capacity() * 2);
    return getByteBuffer(Math.max(minCapacity, growthCapacity));
  }

  protected final int checkCapacity(long capacity) {
    if (capacity > maxCapacity) {
      throw Exceptions.bufferSizeLimitExceeded(capacity, maxCapacity);
    }
    return (int) capacity;
  }

  protected final int checkCharCapacity(long capacity) {
    if (capacity > maxCapacity / 2) {
      throw Exceptions.bufferSizeLimitExceeded(capacity * 2, maxCapacity);
    }
    return (int) capacity;
  }
}
