/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.MessageReader
import net.jqwik.api.ForAll
import net.jqwik.api.constraints.Size
import org.assertj.core.api.Assertions.assertThat

class ReadMap : Example() {
  // -8<- [start:snippet]
  fun read(reader: MessageReader): Map<String, Int> {
    val size = reader.readMapHeader()
    return buildMap(size) {
      repeat(size) {
        val key = reader.readString()
        val value = reader.readInt()
        put(key, value)
      }
    }
  }
  // -8<- [end:snippet]

  @net.jqwik.api.Example
  fun test(@ForAll @Size(max = 16) map: Map<String, Int>) {
    WriteMap().write(writer, map)
    writer.close()
    assertThat(read(reader)).isEqualTo(map)
  }
}
