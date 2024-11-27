/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.example;

import org.junit.jupiter.api.Test;
import io.github.odenix.minipack.core.MessageWriter;

import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class WriteToStream extends Example {
  // -8<- [start:snippet]
  void write(OutputStream stream) throws IOException {
    try (var writer = MessageWriter.of(stream)) {
      writer.write("Hello, MiniPack!");
      writer.write(42);
    }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    write(outStream);
    assertThat(reader.readString()).isEqualTo("Hello, MiniPack!");
    assertThat(reader.readInt()).isEqualTo(42);
  }
}
