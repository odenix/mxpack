/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;

public final class BufferAllocators {
  private BufferAllocators() {}

  public static BufferAllocator defaultAllocator(int minCapacity, int maxCapacity) {
    return new DefaultAllocator(minCapacity, maxCapacity);
  }

  static final class DefaultAllocator implements BufferAllocator {
    private final int minCapacity;
    private final int maxCapacity;
    private @Nullable ByteBuffer currentBuffer;

    DefaultAllocator(int minCapacity, int maxCapacity) {
      if (minCapacity <= 0 || maxCapacity <= 0 || minCapacity > maxCapacity) {
        throw ReaderException.invalidAllocatorCapacitu(minCapacity, maxCapacity);
      }
      this.minCapacity = minCapacity;
      this.maxCapacity = maxCapacity;
    }

    @Override
    public ByteBuffer getArrayBackedBuffer(int requestedCapacity) {
      if (currentBuffer == null || currentBuffer.capacity() < requestedCapacity) {
        if (requestedCapacity > maxCapacity) {
          throw ReaderException.stringTooLarge(requestedCapacity, maxCapacity);
        }
        var newCapacity =
            currentBuffer == null
                ? Math.max(minCapacity, requestedCapacity)
                : Math.max(currentBuffer.capacity() * 2, requestedCapacity);
        currentBuffer = ByteBuffer.allocate(Math.min(maxCapacity, newCapacity));
      }
      return currentBuffer.position(0).limit(requestedCapacity);
    }
  }
}
