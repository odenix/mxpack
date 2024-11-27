/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.minipack.kotlin.MessageReaders
import java.nio.channels.WritableByteChannel
import org.minipack.kotlin.MessageWriters
import java.nio.channels.ReadableByteChannel

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class UseDefaultAllocator : Example() {
  // -8<- [start:snippet]
  class Example {
    fun read(channel: ReadableByteChannel) {
      MessageReaders.of(channel).use { reader -> //(1)
        // read some values
      }
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    val example = Example()
    example.read(inChannel)
  }
}
