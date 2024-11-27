/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin.example

import net.jqwik.api.ForAll
import org.assertj.core.api.Assertions.assertThat
import org.minipack.core.MessageWriter

class WriteArray : Example() {
  // -8<- [start:snippet]
  fun write(writer: MessageWriter, list: List<String>) {
    writer.writeArrayHeader(list.size)
    for (element in list) {
      writer.write(element)
    }
  }
  // -8<- [end:snippet]

  @net.jqwik.api.Example
  fun test(@ForAll list: List<String>) {
    write(writer, list)
    writer.close()
    assertThat(ReadArray().read(reader)).isEqualTo(list)
  }
}
