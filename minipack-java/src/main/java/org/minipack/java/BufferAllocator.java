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
    PooledOptions maxBufferCapacity(int capacity);

    PooledOptions useDirectBuffers(boolean flag);
  }

  interface UnpooledOptions {
    UnpooledOptions maxBufferCapacity(int capacity);

    UnpooledOptions useDirectBuffers(boolean flag);
  }

  interface PooledByteBuffer extends AutoCloseable {
    ByteBuffer value();

    @Override
    void close();
  }

  interface PooledCharBuffer extends AutoCloseable {
    CharBuffer value();

    @Override
    void close();
  }

  PooledByteBuffer getByteBuffer(long minCapacity);

  PooledCharBuffer getCharBuffer(long minCapacity);

  PooledCharBuffer getCharBuffer(double minCapacity);

  PooledByteBuffer ensureRemaining(PooledByteBuffer buffer, long remaining);

  void close();
}
