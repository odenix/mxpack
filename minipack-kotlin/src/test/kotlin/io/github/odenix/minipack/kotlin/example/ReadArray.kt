/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.core.MessageReader
import net.jqwik.api.ForAll

class ReadArray : Example() {
  // -8<- [start:snippet]
  fun read(reader: MessageReader): List<String> {
    val size = reader.readArrayHeader()
    return buildList(size) {
      repeat(size) {
        val element = reader.readString()
        add(element)
      }
    }
  }
  // -8<- [end:snippet]

  @net.jqwik.api.Example
  fun test(@ForAll list: List<String>) {
    WriteArray().test(list)
  }
}
