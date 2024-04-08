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
    Builder minCapacity(int capacity);

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

  CharBuffer charBuffer(long minCapacity);

  ByteBuffer ensureRemaining(ByteBuffer buffer, int remaining);

  void release(ByteBuffer buffer);

  void release(CharBuffer buffer);

  void close();
}
