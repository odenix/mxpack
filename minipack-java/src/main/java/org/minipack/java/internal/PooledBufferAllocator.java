/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.minipack.java.internal.util.LockFreePool;

public final class PooledBufferAllocator extends AbstractBufferAllocator {
  @SuppressWarnings("unchecked")
  private final LockFreePool<ByteBuffer>[] byteBufferBuckets = new LockFreePool[32];

  @SuppressWarnings("unchecked")
  private final LockFreePool<CharBuffer>[] charBufferBuckets = new LockFreePool[32];

  private final AtomicBoolean isClosed = new AtomicBoolean();

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
      byteBufferBuckets[i] = new LockFreePool<>();
      charBufferBuckets[i] = new LockFreePool<>();
    }
  }

  @Override
  public DefaultPooledByteBuffer getByteBuffer(long minCapacity) {
    var capacity = checkCapacity(minCapacity);
    var index = getBucketIndex(capacity);
    var bucket = byteBufferBuckets[index];
    var buffer = bucket.get();
    var result =
        buffer != null
            ? buffer.clear()
            : preferDirect
                ? ByteBuffer.allocateDirect(1 << index)
                : ByteBuffer.allocate(1 << index);
    return new DefaultPooledByteBuffer(result, this);
  }

  @Override
  public DefaultPooledCharBuffer getCharBuffer(long minCapacity) {
    var capacity = checkCharCapacity(minCapacity);
    var index = getBucketIndex(capacity);
    var bucket = charBufferBuckets[index];
    var buffer = bucket.get();
    var result = buffer != null ? buffer.clear() : CharBuffer.allocate(1 << index);
    return new DefaultPooledCharBuffer(result, this);
  }

  @Override
  public DefaultPooledCharBuffer getCharBuffer(double minCapacity) {
    return getCharBuffer((long) Math.ceil(minCapacity));
  }

  @Override
  @SuppressWarnings("DataFlowIssue")
  public void close() {
    if (isClosed.getAndSet(true)) return;
    for (int i = 0; i < 32; i++) {
      byteBufferBuckets[i] = null;
      charBufferBuckets[i] = null;
    }
  }

  void release(DefaultPooledByteBuffer pooled) {
    if (isClosed.get()) return;
    var buffer = pooled.value;
    var index = getBucketIndex(buffer.capacity());
    var bucket = byteBufferBuckets[index];
    bucket.add(buffer);
  }

  void release(DefaultPooledCharBuffer pooled) {
    if (isClosed.get()) return;
    var buffer = pooled.value;
    var index = getBucketIndex(buffer.capacity());
    var bucket = charBufferBuckets[index];
    bucket.add(buffer);
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
