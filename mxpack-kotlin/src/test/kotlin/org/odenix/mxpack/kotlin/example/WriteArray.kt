/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.MessageWriter
import net.jqwik.api.ForAll
import org.assertj.core.api.Assertions.assertThat

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
