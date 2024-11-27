/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.junit.jupiter.api.Test
import org.minipack.core.BufferAllocator
import org.minipack.core.MessageEncoder
import org.minipack.core.MessageWriter
import org.minipack.kotlin.BufferAllocators
import org.minipack.kotlin.MessageEncoders
import org.minipack.kotlin.MessageWriters
import java.io.ByteArrayOutputStream
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel

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
