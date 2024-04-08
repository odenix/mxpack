/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;

public abstract class AbstractBufferAllocator implements BufferAllocator {
  protected final int minCapacity;
  protected final int maxCapacity;
  protected final boolean preferDirect;

  protected AbstractBufferAllocator(Builder builder) {
    minCapacity = builder.minCapacity;
    maxCapacity = builder.maxCapacity;
    preferDirect = builder.preferDirect;
  }

  public static final class Builder implements BufferAllocator.Builder {
    private final boolean isPooled;
    private int minCapacity = 1024 * 8;
    private int maxCapacity = 1024 * 1024;
    private boolean preferDirect;

    public Builder(boolean isPooled) {
      this.isPooled = isPooled;
    }

    @Override
    public Builder minCapacity(int capacity) {
      minCapacity = capacity;
      return this;
    }

    @Override
    public Builder maxCapacity(int capacity) {
      maxCapacity = capacity;
      return this;
    }

    public Builder preferDirect(boolean flag) {
      preferDirect = flag;
      return this;
    }

    @Override
    public BufferAllocator build() {
      return isPooled ? new PooledBufferAllocator(this) : new UnpooledBufferAllocator(this);
    }
  }

  @Override
  public final ByteBuffer ensureRemaining(ByteBuffer buffer, int remaining) {
    if (buffer.remaining() >= remaining) return buffer;
    var minCapacity = checkCapacity(buffer.position() + (long) remaining);
    var growthCapacity = Math.min(maxCapacity, buffer.capacity() * 2);
    var newBuffer = byteBuffer(Math.max(minCapacity, growthCapacity));
    newBuffer.put(buffer.flip());
    release(buffer);
    return newBuffer;
  }

  protected final int checkCapacity(long capacity) {
    if (capacity > maxCapacity) {
      throw Exceptions.bufferSizeLimitExceeded(capacity, maxCapacity);
    }
    return Math.max((int) capacity, minCapacity);
  }

  protected final int checkCharCapacity(long capacity) {
    if (capacity > maxCapacity / 2) {
      throw Exceptions.bufferSizeLimitExceeded(capacity * 2, maxCapacity);
    }
    return Math.max((int) capacity, minCapacity / 2);
  }
}
