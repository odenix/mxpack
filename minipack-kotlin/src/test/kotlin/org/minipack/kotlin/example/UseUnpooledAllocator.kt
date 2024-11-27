/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.channels.WritableByteChannel
import org.minipack.core.BufferAllocator
import org.minipack.kotlin.BufferAllocators
import org.minipack.kotlin.MessageReaders
import org.minipack.kotlin.MessageWriters
import java.nio.channels.ReadableByteChannel

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class UseUnpooledAllocator : Example() {
  // -8<- [start:snippet]
  class Example {
    val allocator: BufferAllocator = BufferAllocators.ofUnpooled( //(1)
      maxByteBufferCapacity = Integer.MAX_VALUE, //(2)
      maxCharBufferCapacity = Integer.MAX_VALUE
    )

    fun read(channel: ReadableByteChannel) {
      MessageReaders.of(channel, allocator = allocator).use { reader -> //(3)
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
