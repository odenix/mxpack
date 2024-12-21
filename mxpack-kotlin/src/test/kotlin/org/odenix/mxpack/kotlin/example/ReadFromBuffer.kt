/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.LeasedByteBuffer
import org.junit.jupiter.api.Test
import org.odenix.mxpack.kotlin.*

@Suppress("UNUSED_VARIABLE")
class ReadFromBuffer : Example() {
  // -8<- [start:snippet]
  fun read(buffer: LeasedByteBuffer) {
    MessageReader(buffer).use { reader ->
      val string = reader.readString()
      val number = reader.readInt()
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    val output = BufferOutput()
    val writer = MessageWriter(output)
    writer.write("Hello, MxPack!")
    writer.write(42)
    writer.close()
    read(output.get())
  }
}
