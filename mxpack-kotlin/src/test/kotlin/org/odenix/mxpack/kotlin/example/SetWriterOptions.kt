/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.kotlin.BufferAllocators
import org.odenix.mxpack.kotlin.MessageEncoders
import org.odenix.mxpack.kotlin.MessageWriters
import java.nio.channels.WritableByteChannel
import org.junit.jupiter.api.Test

class SetWriterOptions : Example() {
  // -8<- [start:snippet]
  fun write(channel: WritableByteChannel) {
    MessageWriters.of(
      channel,
      allocator = BufferAllocators.ofUnpooled(), //(1)
      writeBufferCapacity = 1024 * 8,
      stringEncoder = MessageEncoders.ofStrings(),
      identifierEncoder = MessageEncoders.ofStrings()
    ).use { /* write some values */ }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    write(outChannel)
  }
}
