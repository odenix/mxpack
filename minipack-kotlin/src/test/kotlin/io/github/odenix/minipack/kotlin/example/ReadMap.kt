/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.core.MessageReader
import net.jqwik.api.ForAll

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
  fun test(@ForAll map: Map<String, Int>) {
    WriteMap().test(map)
  }
}
