/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.example;

import org.junit.jupiter.api.Test;
import org.minipack.core.MessageWriter;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import static org.assertj.core.api.Assertions.assertThat;

class WriteToChannel extends Example {
  // -8<- [start:snippet]
  void write(WritableByteChannel channel) throws IOException {
    try (var writer = MessageWriter.of(channel)) {
      writer.write("Hello, MiniPack!");
      writer.write(42);
    }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    write(outChannel);
    assertThat(reader.readString()).isEqualTo("Hello, MiniPack!");
    assertThat(reader.readInt()).isEqualTo(42);
  }
}
