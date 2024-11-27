/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.minipack.core.BufferAllocator;
import org.minipack.core.LeasedByteBuffer;
import org.minipack.core.LeasedCharBuffer;
import org.minipack.core.internal.util.LockFreePool;

import javax.swing.text.html.Option;

/// A [BufferAllocator] that pools buffers.
///
/// MiniPack supports buffer pooling based on the following assumptions:
/// * MiniPack is used with different garbage collectors (JVM, Graal native image, etc.)
/// * pooling heap buffers can be useful
/// * using direct buffers can be useful
/// * pooling direct buffers is useful
///
/// If buffer pooling proves unnecessary for MiniPack,
/// its API and implementation can be simplified in the following ways:
/// * remove [LeasedByteBuffer] and [LeasedCharBuffer]
/// * maybe remove [BufferAllocator]
public final class PooledBufferAllocator extends AbstractBufferAllocator implements BufferAllocator {
  /// Increasing this value increases [LockFreePool] contention for small buffers.
  private static final int MIN_BUCKET_INDEX = 4;

  private final int maxPooledByteBufferCapacity;
  private final int maxPooledCharBufferCapacity;
  private final int maxByteBufferPoolCapacity;
  private final int maxCharBufferPoolCapacity;
  private final boolean preferDirectBuffers;
  private final AtomicInteger currentByteBufferPoolCapacity = new AtomicInteger();
  private final AtomicInteger currentCharBufferPoolCapacity = new AtomicInteger();
  @SuppressWarnings("unchecked")
  private final LockFreePool<ByteBuffer>[] byteBufferBuckets = new LockFreePool[32];
  @SuppressWarnings("unchecked")
  private final LockFreePool<CharBuffer>[] charBufferBuckets = new LockFreePool[32];

  public static PooledBufferAllocator of(Consumer<PooledOptionBuilder> optionHandler) {
    var options = new OptionBuilderImpl();
    optionHandler.accept(options);
    return new PooledBufferAllocator(options);
  }

  private PooledBufferAllocator(OptionBuilderImpl options) {
    super(options);
    maxByteBufferPoolCapacity = options.maxByteBufferPoolCapacity;
    maxCharBufferPoolCapacity = options.maxCharBufferPoolCapacity;
    maxPooledByteBufferCapacity = options.maxPooledByteBufferCapacity;
    maxPooledCharBufferCapacity = options.maxPooledCharBufferCapacity;
    preferDirectBuffers = options.preferDirectBuffers;
    for (var i = MIN_BUCKET_INDEX; i < 32; i++) {
      byteBufferBuckets[i] = new LockFreePool<>();
      charBufferBuckets[i] = new LockFreePool<>();
    }
  }

  @Override
  public LeasedByteBuffer getByteBuffer(int capacity) {
    checkNotClosed();
    checkByteBufferCapacity(capacity);
    if (capacity > maxPooledByteBufferCapacity) {
      // allocate heap buffer with exact requested capacity for unpooled buffer
      return new LeasedByteBufferImpl(ByteBuffer.allocate(capacity), null);
    }
    var index = getBucketIndex(capacity);
    var bucket = byteBufferBuckets[index];
    var buffer = bucket.get();
    if (buffer != null) {
      buffer.clear();
      return new LeasedByteBufferImpl(buffer, this);
    }
    var newCapacity = currentByteBufferPoolCapacity.addAndGet(capacity);
    // decide at buffer allocation time if buffer will be pooled
    if (newCapacity > maxByteBufferPoolCapacity) {
      currentByteBufferPoolCapacity.addAndGet(-capacity);
      // allocate heap buffer with exact requested capacity for unpooled buffer
      return new LeasedByteBufferImpl(ByteBuffer.allocate(capacity), null);
    }
    var newBuffer = preferDirectBuffers
        ? ByteBuffer.allocateDirect(1 << index)
        : ByteBuffer.allocate(1 << index);
    return new LeasedByteBufferImpl(newBuffer, this);
  }

  @Override
  public LeasedCharBuffer getCharBuffer(int capacity) {
    checkNotClosed();
    checkCharBufferCapacity(capacity);
    if (capacity > maxPooledCharBufferCapacity) {
      // allocate buffer with exact requested capacity for unpooled buffer
      return new LeasedCharBufferImpl(CharBuffer.allocate(capacity), null);
    }
    var index = getBucketIndex(capacity);
    var bucket = charBufferBuckets[index];
    var buffer = bucket.get();
    if (buffer != null) {
      buffer.clear();
      return new LeasedCharBufferImpl(buffer, this);
    }
    var newCapacity = currentCharBufferPoolCapacity.addAndGet(capacity);
    // decide at buffer allocation time if buffer will be pooled
    if (newCapacity > maxCharBufferPoolCapacity) {
      currentCharBufferPoolCapacity.addAndGet(-capacity);
      // allocate buffer with exact requested capacity for unpooled buffer
      return new LeasedCharBufferImpl(CharBuffer.allocate(capacity), null);
    }
    return new LeasedCharBufferImpl(CharBuffer.allocate(1 << index), this);
  }

  @Override
  @SuppressWarnings("DataFlowIssue")
  public void close() {
    if (getAndSetClosed()) return;
    for (var i = MIN_BUCKET_INDEX; i < 32; i++) {
      byteBufferBuckets[i] = null;
      charBufferBuckets[i] = null;
    }
  }

  void release(LeasedByteBufferImpl leasedBuffer) {
    if (isClosed()) return;
    var buffer = leasedBuffer.buffer;
    var capacity = buffer.capacity();
    var index = getBucketIndex(capacity);
    var bucket = byteBufferBuckets[index];
    bucket.add(buffer);
  }

  void release(LeasedCharBufferImpl leasedBuffer) {
    if (isClosed()) return;
    var buffer = leasedBuffer.buffer;
    var capacity = buffer.capacity();
    var index = getBucketIndex(capacity);
    var bucket = charBufferBuckets[index];
    bucket.add(buffer);
  }

  private int getBucketIndex(int capacity) {
    var index = capacity == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(capacity - 1);
    return Math.max(index, MIN_BUCKET_INDEX);
  }
}
