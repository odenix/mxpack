/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.core.MessageReader
import io.github.odenix.minipack.kotlin.*
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
    writer.write(255.toUByte())
    writer.write(65535.toUShort())
    writer.write(UInt.MAX_VALUE)
    writer.write(ULong.MAX_VALUE)
    writer.close()
    read(reader)
  }
}
