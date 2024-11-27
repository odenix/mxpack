/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.kotlin.BufferAllocators
import io.github.odenix.minipack.kotlin.MessageDecoders
import io.github.odenix.minipack.kotlin.MessageReaders
import java.nio.channels.ReadableByteChannel
import org.junit.jupiter.api.Test

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
