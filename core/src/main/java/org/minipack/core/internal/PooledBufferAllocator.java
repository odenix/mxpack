/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class PooledBufferAllocator extends AbstractBufferAllocator {
  @SuppressWarnings("unchecked")
  private final Queue<ByteBuffer>[] byteBufferBuckets = new Queue[32];

  @SuppressWarnings("unchecked")
  private final Queue<CharBuffer>[] charBufferBuckets = new Queue[32];

  @SuppressWarnings("unchecked")
  private final Queue<char[]>[] charArrayBuckets = new Queue[32];

  public PooledBufferAllocator(AbstractBufferAllocator.Builder builder) {
    super(builder);
    for (int i = 0; i < 32; i++) {
      byteBufferBuckets[i] = new ConcurrentLinkedQueue<>();
      charBufferBuckets[i] = new ConcurrentLinkedQueue<>();
      charArrayBuckets[i] = new ConcurrentLinkedQueue<>();
    }
  }

  @Override
  public ByteBuffer byteBuffer(long minCapacity) {
    var capacity = checkCapacity(minCapacity);
    var index = 32 - Integer.numberOfLeadingZeros(capacity - 1);
    assert 1 << index >= capacity;
    var bucket = byteBufferBuckets[index];
    var buffer = bucket.poll();
    return buffer != null
        ? buffer
        : preferDirect ? ByteBuffer.allocateDirect(1 << index) : ByteBuffer.allocate(1 << index);
  }

  @Override
  public CharBuffer charBuffer(long minCapacity) {
    var capacity = checkCharCapacity(minCapacity);
    var index = 32 - Integer.numberOfLeadingZeros(capacity - 1);
    var bucket = charBufferBuckets[index];
    var buffer = bucket.poll();
    return buffer != null ? buffer : CharBuffer.allocate(1 << index);
  }

  @Override
  public char[] charArray(long minLength) {
    var capacity = checkCharCapacity(minLength);
    var index = 32 - Integer.numberOfLeadingZeros(capacity - 1);
    var bucket = charArrayBuckets[index];
    var buffer = bucket.poll();
    return buffer != null ? buffer : new char[1 << index];
  }

  @Override
  public void release(ByteBuffer buffer) {
    var index = 32 - Integer.numberOfLeadingZeros(buffer.capacity() - 1);
    var bucket = byteBufferBuckets[index];
    bucket.add(buffer);
  }

  @Override
  public void release(CharBuffer buffer) {
    var index = 32 - Integer.numberOfLeadingZeros(buffer.capacity() - 1);
    var bucket = charBufferBuckets[index];
    bucket.add(buffer);
  }

  @Override
  public void release(char[] buffer) {
    var index = 32 - Integer.numberOfLeadingZeros(buffer.length - 1);
    var bucket = charArrayBuckets[index];
    bucket.add(buffer);
  }

  @Override
  @SuppressWarnings("DataFlowIssue")
  public void close() {
    for (int i = 0; i < 32; i++) {
      byteBufferBuckets[i] = null;
      charBufferBuckets[i] = null;
      charArrayBuckets[i] = null;
    }
  }
}
