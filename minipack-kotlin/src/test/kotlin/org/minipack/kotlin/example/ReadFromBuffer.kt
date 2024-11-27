/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.junit.jupiter.api.Test
import org.minipack.core.LeasedByteBuffer
import org.minipack.kotlin.BufferOutputs
import org.minipack.kotlin.MessageReaders
import org.minipack.kotlin.MessageWriters

@Suppress("UNUSED_VARIABLE")
class ReadFromBuffer : Example() {
  // -8<- [start:snippet]
  fun read(buffer: LeasedByteBuffer) {
    MessageReaders.of(buffer).use { reader ->
      val string = reader.readString()
      val number = reader.readInt()
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    val output = BufferOutputs.of()
    val writer = MessageWriters.of(output)
    writer.write("Hello, MiniPack!")
    writer.write(42)
    writer.close()
    read(output.get())
  }
}
