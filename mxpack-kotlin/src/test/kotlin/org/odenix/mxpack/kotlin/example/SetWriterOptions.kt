/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import java.nio.channels.WritableByteChannel
import org.junit.jupiter.api.Test
import org.odenix.mxpack.kotlin.*

class SetWriterOptions : Example() {
  // -8<- [start:snippet]
  fun write(channel: WritableByteChannel) {
    MessageWriter(
      channel,
      allocator = UnpooledBufferAllocator(), //(1)
      writeBufferCapacity = 1024 * 8,
      stringEncoder = StringEncoder(),
      identifierEncoder = StringEncoder()
    ).use { /* write some values */ }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    write(outChannel)
  }
}
