/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.minipack.core.LeasedByteBuffer
import org.minipack.core.MessageWriter.BufferOutput
import org.minipack.kotlin.MessageReaders
import org.minipack.kotlin.MessageWriters

class WriteToBuffer : Example() {
  // -8<- [start:snippet]
  fun write(): LeasedByteBuffer {
    val output = BufferOutput.of()
    MessageWriters.of(output).use { writer ->
      writer.write("Hello, MiniPack!")
      writer.write(42)
    }
    return output.get()
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    val buffer = write()
    val reader = MessageReaders.of(buffer)
    assertThat(reader.readString()).isEqualTo("Hello, MiniPack!")
    assertThat(reader.readInt()).isEqualTo(42)
  }
}
