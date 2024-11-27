/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.example;

import org.junit.jupiter.api.Test;
import io.github.odenix.minipack.core.LeasedByteBuffer;
import io.github.odenix.minipack.core.MessageReader;
import io.github.odenix.minipack.core.MessageWriter;

import java.io.IOException;

@SuppressWarnings("unused")
class ReadFromBuffer extends Example {
  // -8<- [start:snippet]
  void read(LeasedByteBuffer buffer) throws IOException {
    try (var reader = MessageReader.of(buffer)) {
      var string = reader.readString();
      var number = reader.readInt();
    }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    var output = MessageWriter.BufferOutput.of();
    var writer = MessageWriter.of(output);
    writer.write("Hello, MiniPack!");
    writer.write(42);
    writer.close();
    read(output.get());
  }
}
