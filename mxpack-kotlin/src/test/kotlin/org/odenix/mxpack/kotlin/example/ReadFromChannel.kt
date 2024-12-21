/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import java.nio.channels.ReadableByteChannel
import org.junit.jupiter.api.Test
import org.odenix.mxpack.kotlin.MessageReader

@Suppress("UNUSED_VARIABLE")
class ReadFromChannel : Example() {
  // -8<- [start:snippet]
  fun read(channel: ReadableByteChannel) {
    MessageReader(channel).use { reader ->
      val string = reader.readString()
      val number = reader.readInt()
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    writer.write("Hello, MxPack!")
    writer.write(42)
    writer.close()
    read(inChannel)
  }
}
