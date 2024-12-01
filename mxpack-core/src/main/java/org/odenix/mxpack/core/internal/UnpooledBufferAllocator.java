/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.function.Consumer;

import org.odenix.mxpack.core.BufferAllocator;
import org.odenix.mxpack.core.LeasedByteBuffer;
import org.odenix.mxpack.core.LeasedCharBuffer;

/// A [BufferAllocator] that instantiates a new buffer each time a buffer is requested.
public final class UnpooledBufferAllocator extends AbstractBufferAllocator implements BufferAllocator {
  public static UnpooledBufferAllocator of(Consumer<UnpooledOptionBuilder> optionHandler) {
    var options = new OptionBuilderImpl();
    optionHandler.accept(options);
    return new UnpooledBufferAllocator(options);
  }

  private UnpooledBufferAllocator(OptionBuilderImpl options) {
    super(options);
  }

  @Override
  public LeasedByteBuffer getByteBuffer(int capacity) {
    checkNotClosed();
    checkByteBufferCapacity(capacity);
    var buffer = ByteBuffer.allocate(capacity);
    return new LeasedByteBufferImpl(buffer, null);
  }

  @Override
  public LeasedCharBuffer getCharBuffer(int capacity) {
    checkNotClosed();
    checkCharBufferCapacity(capacity);
    var buffer = CharBuffer.allocate(capacity);
    return new LeasedCharBufferImpl(buffer, null);
  }

  @Override
  public void close() {
    getAndSetClosed();
  }
}
