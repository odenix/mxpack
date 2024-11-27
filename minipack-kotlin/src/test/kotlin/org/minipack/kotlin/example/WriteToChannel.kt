/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.channels.WritableByteChannel
import org.minipack.kotlin.MessageWriters

class WriteToChannel : Example() {
  // -8<- [start:snippet]
  fun write(channel: WritableByteChannel) {
    MessageWriters.of(channel).use { writer ->
      writer.write("Hello, MiniPack!")
      writer.write(42)
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    write(outChannel)
    assertThat(reader.readString()).isEqualTo("Hello, MiniPack!")
    assertThat(reader.readInt()).isEqualTo(42)
  }
}
