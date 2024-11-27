/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.OutputStream
import org.minipack.kotlin.MessageWriters

class WriteToStream : Example() {
  // -8<- [start:snippet]
  fun write(stream: OutputStream) {
    MessageWriters.of(stream).use { writer ->
      writer.write("Hello, MiniPack!")
      writer.write(42)
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    write(outStream)
    writer.close()
    assertThat(reader.readString()).isEqualTo("Hello, MiniPack!")
    assertThat(reader.readInt()).isEqualTo(42)
  }
}
