/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.minipack.core.MessageWriter
import org.minipack.kotlin.*

class KotlinWriteUnsigned : Example() {
  @Suppress("UNUSED_VARIABLE")
  // -8<- [start:snippet]
  fun write(writer: MessageWriter) {
    writer.write(255.toUByte())
    writer.write(65535.toUShort())
    writer.write(UInt.MAX_VALUE)
    writer.write(ULong.MAX_VALUE)
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    write(writer)
    writer.close()
    assertThat(reader.readUByteKotlin()).isEqualTo(255.toUByte())
    assertThat(reader.readUShortKotlin()).isEqualTo(65535.toUShort())
    assertThat(reader.readUIntKotlin()).isEqualTo(UInt.MAX_VALUE)
    assertThat(reader.readULongKotlin()).isEqualTo(ULong.MAX_VALUE)
  }
}
