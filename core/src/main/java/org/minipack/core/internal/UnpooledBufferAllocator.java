/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import org.minipack.core.BufferAllocator;

public final class UnpooledBufferAllocator implements BufferAllocator {
  private final int minCapacity;
  private final int maxCapacity;
  private final boolean preferDirect;

  public static final class Builder implements BufferAllocator.Builder {
    private int minCapacity = 1024 * 8;
    private int maxCapacity = 1024 * 1024;
    private boolean preferDirect;

    @Override
    public BufferAllocator.Builder minCapacity(int capacity) {
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
      return new UnpooledBufferAllocator(this);
    }
  }

  public UnpooledBufferAllocator(Builder builder) {
    minCapacity = builder.minCapacity;
    maxCapacity = builder.maxCapacity;
    preferDirect = builder.preferDirect;
  }

  @Override
  public ByteBuffer byteBuffer(long minCapacity) {
    var capacity = checkCapacity(minCapacity);
    return preferDirect ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
  }

  @Override
  public CharBuffer charBuffer(long minCapacity) {
    var capacity = checkCharCapacity(minCapacity);
    return CharBuffer.allocate(capacity);
  }

  @Override
  public char[] charArray(long minLength) {
    var capacity = checkCharCapacity(minLength);
    return new char[capacity];
  }

  @Override
  public ByteBuffer ensureRemaining(ByteBuffer buffer, int remaining) {
    if (buffer.remaining() >= remaining) return buffer;
    var minCapacity = checkCapacity(buffer.position() + (long) remaining);
    var growthCapacity = Math.min(maxCapacity, buffer.capacity() * 2);
    var newBuffer = byteBuffer(Math.max(minCapacity, growthCapacity));
    newBuffer.put(buffer.flip());
    return newBuffer;
  }

  @Override
  public void release(ByteBuffer buffer) {} // nothing to do

  @Override
  public void release(CharBuffer buffer) {} // nothing to do

  @Override
  public void release(char[] buffer) {} // nothing to do

  @Override
  public void close() {} // nothing to do

  private int checkCapacity(long capacity) {
    if (capacity > maxCapacity) {
      throw Exceptions.bufferSizeLimitExceeded(capacity, maxCapacity);
    }
    return Math.max((int) capacity, minCapacity);
  }

  private int checkCharCapacity(long capacity) {
    if (capacity > maxCapacity / 2) {
      throw Exceptions.bufferSizeLimitExceeded(capacity * 2, maxCapacity);
    }
    return Math.max((int) capacity, minCapacity / 2);
  }
}
