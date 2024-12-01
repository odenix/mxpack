/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.kotlin.MessageReaders
import java.nio.channels.ReadableByteChannel
import org.junit.jupiter.api.Test

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
