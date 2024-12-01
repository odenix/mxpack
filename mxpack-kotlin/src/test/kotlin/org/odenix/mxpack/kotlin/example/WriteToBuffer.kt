/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.LeasedByteBuffer
import org.odenix.mxpack.kotlin.MessageReaders
import org.odenix.mxpack.kotlin.MessageWriters
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.odenix.mxpack.core.MessageOutput
import org.odenix.mxpack.kotlin.MessageOutputs

class WriteToBuffer : Example() {
  // -8<- [start:snippet]
  fun write(): LeasedByteBuffer {
    val output = MessageOutputs.ofBuffer()
    MessageWriters.of(output).use { writer ->
      writer.write("Hello, MxPack!")
      writer.write(42)
    }
    return output.get()
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    val buffer = write()
    val reader = MessageReaders.of(buffer)
    assertThat(reader.readString()).isEqualTo("Hello, MxPack!")
    assertThat(reader.readInt()).isEqualTo(42)
  }
}
