/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import java.nio.channels.ReadableByteChannel
import org.junit.jupiter.api.Test
import org.odenix.mxpack.kotlin.*

class SetReaderOptions : Example() {
  // -8<- [start:snippet]
  fun read(channel: ReadableByteChannel) {
    MessageReader(
      channel,
      allocator = UnpooledAllocator(), //(1)
      readBufferCapacity = 1024 * 8,
      stringDecoder = StringDecoder(),
      identifierDecoder = StringDecoder(),
    ).use { /* read some values */ }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    read(inChannel)
  }
}
