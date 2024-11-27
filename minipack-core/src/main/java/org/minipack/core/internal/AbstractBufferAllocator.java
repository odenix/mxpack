/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import org.minipack.core.BufferAllocator;

/// Common base class for [BufferAllocator] implementations.
public abstract sealed class AbstractBufferAllocator extends AbstractCloseable implements BufferAllocator
    permits PooledBufferAllocator, UnpooledBufferAllocator {
  private final int maxByteBufferCapacity;
  private final int maxCharBufferCapacity;

  public static final class OptionBuilderImpl implements UnpooledOptionBuilder, PooledOptionBuilder {
    int maxByteBufferCapacity = Integer.MAX_VALUE;
    int maxCharBufferCapacity = Integer.MAX_VALUE;
    int maxPooledByteBufferCapacity = 1024 * 1024;
    int maxPooledCharBufferCapacity = 1024 * 512;
    int maxByteBufferPoolCapacity = 1024 * 1024 * 64;
    int maxCharBufferPoolCapacity = 1024 * 1024 * 32;
    boolean preferDirectBuffers = false;

    protected OptionBuilderImpl() {}

    @Override
    public OptionBuilderImpl maxByteBufferCapacity(int capacity) {
      maxByteBufferCapacity = capacity;
      return this;
    }

    @Override
    public OptionBuilderImpl maxCharBufferCapacity(int capacity) {
      maxCharBufferCapacity = capacity;
      return this;
    }

    public OptionBuilderImpl maxPooledByteBufferCapacity(int capacity) {
      maxPooledByteBufferCapacity = capacity;
      return this;
    }

    public OptionBuilderImpl maxPooledCharBufferCapacity(int capacity) {
      maxPooledCharBufferCapacity = capacity;
      return this;
    }

    @Override
    public OptionBuilderImpl maxByteBufferPoolCapacity(int capacity) {
      maxByteBufferPoolCapacity = capacity;
      return this;
    }

    @Override
    public OptionBuilderImpl maxCharBufferPoolCapacity(int capacity) {
      maxCharBufferPoolCapacity = capacity;
      return this;
    }

    @Override
    public OptionBuilderImpl preferDirectBuffers(boolean flag) {
      preferDirectBuffers = flag;
      return this;
    }
  }

  protected AbstractBufferAllocator(OptionBuilderImpl options) {
    this.maxByteBufferCapacity = options.maxByteBufferCapacity;
    this.maxCharBufferCapacity = options.maxCharBufferCapacity;
  }

  @Override
  public final int maxByteBufferCapacity() {
    return maxByteBufferCapacity;
  }

  @Override
  public final int maxCharBufferCapacity() {
    return maxCharBufferCapacity;
  }

  protected final void checkByteBufferCapacity(int capacity) {
    if (capacity < 0) {
      throw Exceptions.negativeArgument(capacity);
    }
    if (capacity > maxByteBufferCapacity) {
      throw Exceptions.bufferSizeLimitExceeded(capacity, maxByteBufferCapacity);
    }
  }

  protected final void checkCharBufferCapacity(int capacity) {
    if (capacity < 0) {
      throw Exceptions.negativeArgument(capacity);
    }
    if (capacity > maxCharBufferCapacity) {
      throw Exceptions.bufferSizeLimitExceeded(capacity, maxCharBufferCapacity);
    }
  }
}
