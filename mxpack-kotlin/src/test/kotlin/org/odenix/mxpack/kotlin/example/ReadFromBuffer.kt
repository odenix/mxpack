/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.LeasedByteBuffer
import org.odenix.mxpack.kotlin.MessageOutputs
import org.odenix.mxpack.kotlin.MessageReaders
import org.odenix.mxpack.kotlin.MessageWriters
import org.junit.jupiter.api.Test

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
    val output = MessageOutputs.ofBuffer()
    val writer = MessageWriters.of(output)
    writer.write("Hello, MxPack!")
    writer.write(42)
    writer.close()
    read(output.get())
  }
}
