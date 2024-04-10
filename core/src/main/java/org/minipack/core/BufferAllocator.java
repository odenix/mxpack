/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import org.minipack.core.internal.AbstractBufferAllocator;

public interface BufferAllocator extends Closeable {
  interface Builder {
    Builder maxCapacity(int capacity);

    Builder preferDirect(boolean flag);

    BufferAllocator build();
  }

  static Builder pooled() {
    return new AbstractBufferAllocator.Builder(true);
  }

  static Builder unpooled() {
    return new AbstractBufferAllocator.Builder(false);
  }

  ByteBuffer byteBuffer(long minCapacity);

  ByteBuffer newByteBuffer(long capacity);

  CharBuffer charBuffer(long minCapacity);

  CharBuffer newCharBuffer(long minCapacity);

  CharBuffer charBuffer(double minCapacity);

  ByteBuffer ensureRemaining(ByteBuffer buffer, long remaining);

  void release(ByteBuffer buffer);

  void release(CharBuffer buffer);

  void close();
}
