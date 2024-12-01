/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.MessageWriter
import org.odenix.mxpack.kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinWriteUnsigned : Example() {
  // -8<- [start:snippet]
  fun write(writer: MessageWriter) {
    writer.write(UByte.MAX_VALUE)
    writer.write(UShort.MAX_VALUE)
    writer.write(UInt.MAX_VALUE)
    writer.write(ULong.MAX_VALUE)
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    write(writer)
    writer.close()
    assertThat(reader.readUByteKotlin()).isEqualTo(UByte.MAX_VALUE)
    assertThat(reader.readUShortKotlin()).isEqualTo(UShort.MAX_VALUE)
    assertThat(reader.readUIntKotlin()).isEqualTo(UInt.MAX_VALUE)
    assertThat(reader.readULongKotlin()).isEqualTo(ULong.MAX_VALUE)
  }
}
