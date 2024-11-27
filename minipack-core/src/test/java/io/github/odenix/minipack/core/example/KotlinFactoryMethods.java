/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.example;

import org.junit.jupiter.api.Test;
import io.github.odenix.minipack.core.BufferAllocator;

class KotlinFactoryMethods extends Example {
  @SuppressWarnings("unused")
  void run() {
    // -8<- [start:snippet]
    var allocator = BufferAllocator.ofPooled(options -> options
        .maxByteBufferCapacity(1024 * 1024)
        .maxByteBufferPoolCapacity(1024 * 1024 * 32)
    );
    // -8<- [end:snippet]
  }

  @Test
  void test() {
    run();
  }
}
