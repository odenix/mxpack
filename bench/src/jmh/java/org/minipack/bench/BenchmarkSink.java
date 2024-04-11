/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench;

import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageSink;

public final class BenchmarkSink extends MessageSink {
  public BenchmarkSink(ByteBuffer buffer, BufferAllocator allocator) {
    super(allocator, buffer);
  }

  @Override
  protected void doWrite(ByteBuffer buffer) {}

  @Override
  protected void doWrite(ByteBuffer... buffers) {}

  @Override
  protected void doFlush() {}

  @Override
  protected void doClose() {}
}
