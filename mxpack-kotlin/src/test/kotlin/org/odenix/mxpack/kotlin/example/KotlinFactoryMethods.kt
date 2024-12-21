/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.junit.jupiter.api.Test
import org.odenix.mxpack.kotlin.PooledAllocator

class KotlinFactoryMethods : Example() {
  @Suppress("UNUSED_VARIABLE")
  fun run() {
    // -8<- [start:snippet]
    val allocator = PooledAllocator(
      maxByteBufferCapacity = 1024 * 1024,
      maxByteBufferPoolCapacity = 1024 * 1024 * 32
    )
    // -8<- [end:snippet]
  }

  @Test
  fun test() {
    run()
  }
}
