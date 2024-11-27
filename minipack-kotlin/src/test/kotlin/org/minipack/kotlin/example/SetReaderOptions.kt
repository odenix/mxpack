/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.junit.jupiter.api.Test
import org.minipack.core.BufferAllocator
import org.minipack.core.MessageDecoder
import org.minipack.core.MessageReader
import org.minipack.kotlin.BufferAllocators
import org.minipack.kotlin.MessageDecoders
import org.minipack.kotlin.MessageReaders
import java.io.ByteArrayInputStream
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

class SetReaderOptions : Example() {
  // -8<- [start:snippet]
  fun read(channel: ReadableByteChannel) {
    MessageReaders.of(
      channel,
      allocator = BufferAllocators.ofUnpooled(), //(1)
      readBufferCapacity = 1024 * 8,
      stringDecoder = MessageDecoders.ofStrings(),
      identifierDecoder = MessageDecoders.ofStrings(),
    ).use { /* read some values */ }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    read(inChannel)
  }
}
