/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PooledBufferAllocatorTest {
  @Test
  void queryOptions() {
    var allocator = BufferAllocator.ofPooled(options -> options
        .maxByteBufferCapacity(123)
        .maxCharBufferCapacity(456)
    );
    assertThat(allocator.maxByteBufferCapacity()).isEqualTo(123);
    assertThat(allocator.maxCharBufferCapacity()).isEqualTo(456);
  }

  @Test
  void enforcesMaxByteBufferCapacity() {
    var allocator = BufferAllocator.ofPooled(options -> options.maxByteBufferCapacity(123));
    assertThatExceptionOfType(MxPackException.SizeLimitExceeded.class)
        .isThrownBy(() -> allocator.getByteBuffer(124));
  }

  @Test
  void enforcesMaxCharBufferCapacity() {
    var allocator = BufferAllocator.ofPooled(options -> options.maxCharBufferCapacity(123));
    assertThatExceptionOfType(MxPackException.SizeLimitExceeded.class)
        .isThrownBy(() -> allocator.getCharBuffer(124));
  }

  @Test
  void supportsDirectByteBuffers() {
    var allocator = BufferAllocator.ofPooled(options -> options.preferDirectBuffers(true));
    var buffer = allocator.getByteBuffer(123);
    assertThat(buffer.get().isDirect()).isTrue();
  }

  @Test
  void returnsUnpooledHeapBufferIfPoolCapacityExceeded() {
    var allocator = BufferAllocator.ofPooled(options -> options
        .maxByteBufferPoolCapacity(123)
        .preferDirectBuffers(true));
    var buffer = allocator.getByteBuffer(124);
    assertThat(buffer.get().isDirect()).isFalse();
  }
}
