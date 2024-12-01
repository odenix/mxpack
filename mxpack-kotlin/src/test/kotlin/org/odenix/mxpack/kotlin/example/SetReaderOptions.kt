/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.kotlin.BufferAllocators
import org.odenix.mxpack.kotlin.MessageDecoders
import org.odenix.mxpack.kotlin.MessageReaders
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
