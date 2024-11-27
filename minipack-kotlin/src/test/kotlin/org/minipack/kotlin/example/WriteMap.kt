/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import net.jqwik.api.ForAll
import org.assertj.core.api.Assertions.assertThat
import org.minipack.core.MessageWriter

class WriteMap : Example() {
  // -8<- [start:snippet]
  fun write(writer: MessageWriter, map: Map<String, Int>) {
    writer.writeMapHeader(map.size)
    for ((key, value) in map) {
      writer.write(key)
      writer.write(value)
    }
  }
  // -8<- [end:snippet]

  @net.jqwik.api.Example
  fun test(@ForAll map: Map<String, Int>) {
    write(writer, map)
    writer.close()
    assertThat(ReadMap().read(reader)).isEqualTo(map)
  }
}
