/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.junit.jupiter.api.Test
import java.io.InputStream
import org.minipack.kotlin.MessageReaders

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
    writer.write("Hello, MiniPack!")
    writer.write(42)
    writer.close()
    read(inStream)
  }
}
