/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UnpooledBufferAllocatorTest {
  @Test
  void queryOptions() {
    var allocator = BufferAllocator.ofUnpooled(options -> options
        .maxByteBufferCapacity(123)
        .maxCharBufferCapacity(456)
    );
    assertThat(allocator.maxByteBufferCapacity()).isEqualTo(123);
    assertThat(allocator.maxCharBufferCapacity()).isEqualTo(456);
  }

  @Test
  void enforcesMaxByteBufferCapacity() {
    var allocator = BufferAllocator.ofUnpooled(options -> options.maxByteBufferCapacity(123));
    assertThatExceptionOfType(MxPackException.SizeLimitExceeded.class)
        .isThrownBy(() -> allocator.getByteBuffer(124));
  }

  @Test
  void enforcesMaxCharBufferCapacity() {
    var allocator = BufferAllocator.ofUnpooled(options -> options.maxCharBufferCapacity(123));
    assertThatExceptionOfType(MxPackException.SizeLimitExceeded.class)
        .isThrownBy(() -> allocator.getCharBuffer(124));
  }
}
