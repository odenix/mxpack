/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.kotlin.BufferAllocators
import io.github.odenix.minipack.kotlin.MessageEncoders
import io.github.odenix.minipack.kotlin.MessageWriters
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
