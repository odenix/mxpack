/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public final class PooledBufferAllocator extends AbstractBufferAllocator {
  @SuppressWarnings("unchecked")
  private final Queue<ByteBuffer>[] byteBufferBuckets = new Queue[32];

  @SuppressWarnings("unchecked")
  private final Queue<CharBuffer>[] charBufferBuckets = new Queue[32];

  private static final class DefaultPooledOptions implements PooledOptions {
    private int maxCapacity = 1024 * 1024;
    private boolean directBuffers = false;

    @Override
    public PooledOptions maxBufferCapacity(int capacity) {
      maxCapacity = capacity;
      return this;
    }

    @Override
    public PooledOptions useDirectBuffers(boolean flag) {
      directBuffers = flag;
      return this;
    }
  }

  public PooledBufferAllocator() {
    this(options -> {});
  }

  public PooledBufferAllocator(Consumer<PooledOptions> consumer) {
    super(createOptions(consumer).maxCapacity, createOptions(consumer).directBuffers);
    for (int i = 0; i < 32; i++) {
      byteBufferBuckets[i] = new ConcurrentLinkedQueue<>();
      charBufferBuckets[i] = new ConcurrentLinkedQueue<>();
    }
  }

  @Override
  public ByteBuffer acquireByteBuffer(long minCapacity) {
    var capacity = checkCapacity(minCapacity);
    var index = getBucketIndex(capacity);
    var bucket = byteBufferBuckets[index];
    var buffer = bucket.poll();
    return buffer != null
        ? buffer.clear()
        : preferDirect ? ByteBuffer.allocateDirect(1 << index) : ByteBuffer.allocate(1 << index);
  }

  @Override
  public CharBuffer acquireCharBuffer(long minCapacity) {
    var capacity = checkCharCapacity(minCapacity);
    var index = getBucketIndex(capacity);
    var bucket = charBufferBuckets[index];
    var buffer = bucket.poll();
    return buffer != null ? buffer.clear() : CharBuffer.allocate(1 << index);
  }

  @Override
  public void release(ByteBuffer buffer) {
    var index = getBucketIndex(buffer.capacity());
    var bucket = byteBufferBuckets[index];
    bucket.add(buffer);
  }

  @Override
  public void release(CharBuffer buffer) {
    var index = getBucketIndex(buffer.capacity());
    var bucket = charBufferBuckets[index];
    bucket.add(buffer);
  }

  @Override
  @SuppressWarnings("DataFlowIssue")
  public void close() {
    for (int i = 0; i < 32; i++) {
      byteBufferBuckets[i] = null;
      charBufferBuckets[i] = null;
    }
  }

  private static DefaultPooledOptions createOptions(Consumer<PooledOptions> consumer) {
    var options = new DefaultPooledOptions();
    consumer.accept(options);
    return options;
  }

  private int getBucketIndex(int capacity) {
    return capacity == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(capacity - 1);
  }
}
