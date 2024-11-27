/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.core.LeasedByteBuffer
import io.github.odenix.minipack.core.MessageWriter.BufferOutput
import io.github.odenix.minipack.kotlin.MessageReaders
import io.github.odenix.minipack.kotlin.MessageWriters
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
