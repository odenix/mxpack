/*
 * Copyright 2024 the minipack project authors
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
  public int readAny(ByteBuffer buffer, int minBytesHint) {
    return -1;
  }

  @Override
  public void close() {} // nothing to do
}
