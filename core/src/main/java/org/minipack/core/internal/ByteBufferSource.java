/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageSource;

public final class ByteBufferSource extends MessageSource {
  public ByteBufferSource(ByteBuffer buffer, BufferAllocator allocator) {
    super(allocator, buffer);
  }

  @Override
  protected int doRead(ByteBuffer buffer, int minBytesHint) {
    return -1;
  }

  @Override
  protected void doSkip(int length) {
    throw Exceptions.unreachableCode();
  }

  @Override
  protected void doClose() {} // nothing to do
}
