/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.channels.ReadableByteChannel
import org.minipack.kotlin.MessageReaders

@Suppress("UNUSED_VARIABLE")
class ReadFromChannel : Example() {
  // -8<- [start:snippet]
  fun read(channel: ReadableByteChannel) {
    MessageReaders.of(channel).use { reader ->
      val string = reader.readString()
      val number = reader.readInt()
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    writer.write("Hello, MiniPack!")
    writer.write(42)
    writer.close()
    read(inChannel)
  }
}
