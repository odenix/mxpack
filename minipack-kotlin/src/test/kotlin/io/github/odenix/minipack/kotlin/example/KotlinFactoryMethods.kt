/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.kotlin.BufferAllocators
import org.junit.jupiter.api.Test

class KotlinFactoryMethods : Example() {
  @Suppress("UNUSED_VARIABLE")
  fun run() {
    // -8<- [start:snippet]
    val allocator = BufferAllocators.ofPooled(
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
