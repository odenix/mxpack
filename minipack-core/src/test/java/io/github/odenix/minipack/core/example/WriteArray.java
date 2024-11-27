/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.example;

import net.jqwik.api.ForAll;
import io.github.odenix.minipack.core.MessageWriter;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WriteArray extends Example {
  // -8<- [start:snippet]
  void write(MessageWriter writer, List<String> list) throws IOException {
    writer.writeArrayHeader(list.size());
    for (var element : list) {
      writer.write(element);
    }
  }
  // -8<- [end:snippet]

  @net.jqwik.api.Example
  void test(@ForAll List<String> list) throws IOException {
    write(writer, list);
    writer.close();
    assertThat(new ReadArray().read(reader)).isEqualTo(list);
  }
}
