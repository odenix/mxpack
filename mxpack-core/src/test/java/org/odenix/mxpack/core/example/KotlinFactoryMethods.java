/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import org.junit.jupiter.api.Test;
import org.odenix.mxpack.core.BufferAllocator;

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
