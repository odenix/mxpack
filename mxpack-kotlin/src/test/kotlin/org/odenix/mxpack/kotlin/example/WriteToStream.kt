/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import java.io.OutputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.odenix.mxpack.kotlin.MessageWriter

class WriteToStream : Example() {
  // -8<- [start:snippet]
  fun write(stream: OutputStream) {
    MessageWriter(stream).use { writer ->
      writer.write("Hello, MxPack!")
      writer.write(42)
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    write(outStream)
    writer.close()
    assertThat(reader.readString()).isEqualTo("Hello, MxPack!")
    assertThat(reader.readInt()).isEqualTo(42)
  }
}
