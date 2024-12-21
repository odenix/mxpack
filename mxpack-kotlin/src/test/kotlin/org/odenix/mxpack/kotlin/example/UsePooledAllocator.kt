/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.BufferAllocator
import java.nio.channels.ReadableByteChannel
import org.junit.jupiter.api.Test
import org.odenix.mxpack.kotlin.MessageReader
import org.odenix.mxpack.kotlin.PooledBufferAllocator

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class UsePooledAllocator : Example() {
  // -8<- [start:snippet]
  class Example {
    val allocator: BufferAllocator = PooledBufferAllocator( //(1)
      maxByteBufferCapacity = Integer.MAX_VALUE, //(2)
      maxCharBufferCapacity = Integer.MAX_VALUE,
      maxPooledByteBufferCapacity = 1024 * 1024,
      maxPooledCharBufferCapacity = 1024 * 512,
      maxByteBufferPoolCapacity = 1024 * 1024 * 64,
      maxCharBufferPoolCapacity = 1024 * 1024 * 32,
      preferDirectBuffers = false
    )

    fun read(channel: ReadableByteChannel) {
      MessageReader(channel, allocator = allocator).use { reader -> //(3)
        // read some values
      }
    }

    fun close() {
      allocator.close() //(4)
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    val example = Example()
    example.read(inChannel)
    example.close()
  }
}
