/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.function.Consumer;
import org.minipack.java.internal.PooledBufferAllocator;
import org.minipack.java.internal.UnpooledBufferAllocator;

public interface BufferAllocator extends Closeable {
  static BufferAllocator ofPooled() {
    return new PooledBufferAllocator();
  }

  static BufferAllocator ofPooled(Consumer<PooledOptions> consumer) {
    return new PooledBufferAllocator(consumer);
  }

  static BufferAllocator ofUnpooled() {
    return new UnpooledBufferAllocator();
  }

  static BufferAllocator ofUnpooled(Consumer<UnpooledOptions> consumer) {
    return new UnpooledBufferAllocator(consumer);
  }

  interface PooledOptions {
    PooledOptions maxCapacity(int capacity);

    PooledOptions directBuffers(boolean flag);
  }

  interface UnpooledOptions {
    UnpooledOptions maxCapacity(int capacity);

    @SuppressWarnings("UnusedReturnValue")
    UnpooledOptions directBuffers(boolean flag);
  }

  ByteBuffer acquireByteBuffer(long minCapacity);

  ByteBuffer newByteBuffer(long capacity);

  CharBuffer acquireCharBuffer(long minCapacity);

  CharBuffer acquireCharBuffer(double minCapacity);

  CharBuffer newCharBuffer(long minCapacity);

  ByteBuffer ensureRemaining(ByteBuffer buffer, long remaining);

  void release(ByteBuffer buffer);

  void release(CharBuffer buffer);

  void close();
}
