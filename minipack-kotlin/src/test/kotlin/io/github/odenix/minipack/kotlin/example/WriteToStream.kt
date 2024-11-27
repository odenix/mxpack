/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.kotlin.MessageWriters
import java.io.OutputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
