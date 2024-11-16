/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.function.Consumer;

public final class UnpooledBufferAllocator extends AbstractBufferAllocator {
  private static final class DefaultUnpooledOptions implements UnpooledOptions {
    private int maxCapacity = 1024 * 1024;
    private boolean directBuffers = false;

    @Override
    public UnpooledOptions maxBufferCapacity(int capacity) {
      maxCapacity = capacity;
      return this;
    }

    @Override
    public UnpooledOptions useDirectBuffers(boolean flag) {
      directBuffers = flag;
      return this;
    }
  }

  public UnpooledBufferAllocator() {
    this(options -> {});
  }

  public UnpooledBufferAllocator(Consumer<UnpooledOptions> consumer) {
    super(createOptions(consumer).maxCapacity, createOptions(consumer).directBuffers);
  }

  @Override
  public ByteBuffer acquireByteBuffer(long minCapacity) {
    return newByteBuffer(minCapacity);
  }

  @Override
  public CharBuffer acquireCharBuffer(long minCapacity) {
    return newCharBuffer(minCapacity);
  }

  @Override
  public void release(ByteBuffer buffer) {} // nothing to do

  @Override
  public void release(CharBuffer buffer) {} // nothing to do

  @Override
  public void close() {} // nothing to do

  private static DefaultUnpooledOptions createOptions(Consumer<UnpooledOptions> consumer) {
    var options = new DefaultUnpooledOptions();
    consumer.accept(options);
    return options;
  }
}
