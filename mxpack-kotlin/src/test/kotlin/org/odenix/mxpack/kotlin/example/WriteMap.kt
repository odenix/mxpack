/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.MessageWriter
import net.jqwik.api.ForAll
import net.jqwik.api.constraints.Size
import org.assertj.core.api.Assertions.assertThat

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
  fun test(@ForAll @Size(max = 16) map: Map<String, Int>) {
    write(writer, map)
    writer.close()
    assertThat(ReadMap().read(reader)).isEqualTo(map)
  }
}
