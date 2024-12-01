/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.kotlin.MessageWriters
import java.nio.channels.WritableByteChannel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WriteToChannel : Example() {
  // -8<- [start:snippet]
  fun write(channel: WritableByteChannel) {
    MessageWriters.of(channel).use { writer ->
      writer.write("Hello, MxPack!")
      writer.write(42)
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    write(outChannel)
    assertThat(reader.readString()).isEqualTo("Hello, MxPack!")
    assertThat(reader.readInt()).isEqualTo(42)
  }
}
