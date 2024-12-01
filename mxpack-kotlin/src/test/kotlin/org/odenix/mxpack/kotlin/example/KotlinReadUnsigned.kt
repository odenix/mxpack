/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.MessageReader
import org.odenix.mxpack.kotlin.*
import org.junit.jupiter.api.Test

class KotlinReadUnsigned : Example() {
  @Suppress("UNUSED_VARIABLE")
  // -8<- [start:snippet]
  fun read(reader: MessageReader) {
    val num1: UByte = reader.readUByteKotlin()
    val num2: UShort = reader.readUShortKotlin()
    var num3: UInt = reader.readUIntKotlin()
    var num4: ULong = reader.readULongKotlin()
  }
  // -8<- [end:snippet]

  @Test
  fun test() {
    writer.write(UByte.MAX_VALUE)
    writer.write(UShort.MAX_VALUE)
    writer.write(UInt.MAX_VALUE)
    writer.write(ULong.MAX_VALUE)
    writer.close()
    read(reader)
  }
}
