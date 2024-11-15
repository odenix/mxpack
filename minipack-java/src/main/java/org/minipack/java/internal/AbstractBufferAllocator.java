/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import org.minipack.java.BufferAllocator;

public abstract class AbstractBufferAllocator implements BufferAllocator {
  protected final int maxCapacity;
  protected final boolean preferDirect;

  protected AbstractBufferAllocator(int maxCapacity, boolean preferDirect) {
    this.maxCapacity = maxCapacity;
    this.preferDirect = preferDirect;
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
  public final CharBuffer acquireCharBuffer(double minCapacity) {
    return acquireCharBuffer((long) Math.ceil(minCapacity));
  }

  @Override
  public final ByteBuffer ensureRemaining(ByteBuffer buffer, long remaining) {
    if (buffer.remaining() >= remaining) return buffer;
    var minCapacity = checkCapacity(buffer.position() + remaining);
    var growthCapacity = Math.min(maxCapacity, buffer.capacity() * 2);
    var newBuffer = acquireByteBuffer(Math.max(minCapacity, growthCapacity));
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
