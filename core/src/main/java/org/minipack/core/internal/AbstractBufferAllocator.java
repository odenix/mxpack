/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import org.minipack.core.BufferAllocator;

public abstract class AbstractBufferAllocator implements BufferAllocator {
  protected final int maxCapacity;
  protected final boolean preferDirect;

  protected AbstractBufferAllocator(Builder builder) {
    maxCapacity = builder.maxCapacity;
    preferDirect = builder.preferDirect;
  }

  public static final class Builder implements BufferAllocator.Builder {
    private final boolean isPooled;
    private int maxCapacity = 1024 * 1024;
    private boolean preferDirect;

    public Builder(boolean isPooled) {
      this.isPooled = isPooled;
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
  public final ByteBuffer newByteBuffer(long capacity) {
    var cap = checkCapacity(capacity);
    return preferDirect ? ByteBuffer.allocateDirect(cap) : ByteBuffer.allocate(cap);
  }

  @Override
  public final CharBuffer newCharBuffer(long capacity) {
    var cap = checkCharCapacity(capacity);
    return CharBuffer.allocate(cap);
  }

  @Override
  public final CharBuffer charBuffer(double minCapacity) {
    return charBuffer((long) Math.ceil(minCapacity));
  }

  @Override
  public final ByteBuffer ensureRemaining(ByteBuffer buffer, long remaining) {
    if (buffer.remaining() >= remaining) return buffer;
    var minCapacity = checkCapacity(buffer.position() + remaining);
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
    return (int) capacity;
  }

  protected final int checkCharCapacity(long capacity) {
    if (capacity > maxCapacity / 2) {
      throw Exceptions.bufferSizeLimitExceeded(capacity * 2, maxCapacity);
    }
    return (int) capacity;
  }
}
