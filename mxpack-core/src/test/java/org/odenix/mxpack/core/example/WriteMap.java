/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.Size;
import org.odenix.mxpack.core.MessageWriter;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WriteMap extends Example {
  // -8<- [start:snippet]
  void write(MessageWriter writer, Map<String, Integer> map) throws IOException {
    writer.writeMapHeader(map.size());
    for (var entry : map.entrySet()) {
      writer.write(entry.getKey());
      writer.write(entry.getValue());
    }
  }
  // -8<- [end:snippet]

  @net.jqwik.api.Example
  void test(@ForAll @Size(max = 16) Map<String, Integer> map) throws IOException {
    write(writer, map);
    writer.close();
    assertThat(new ReadMap().read(reader)).isEqualTo(map);
  }
}
