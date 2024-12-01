/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.kotlin.MessageReaders
import java.io.InputStream
import org.junit.jupiter.api.Test

@Suppress("UNUSED_VARIABLE")
class ReadFromStream : Example() {
  // -8<- [start:snippet]
  fun read(stream: InputStream) {
    MessageReaders.of(stream).use { reader ->
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
    read(inStream)
  }
}
