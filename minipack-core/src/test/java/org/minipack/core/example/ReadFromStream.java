/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.example;

import org.junit.jupiter.api.Test;
import org.minipack.core.MessageReader;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unused")
class ReadFromStream extends Example {
  // -8<- [start:snippet]
  void read(InputStream stream) throws IOException {
    try (var reader = MessageReader.of(stream)) {
      var string = reader.readString();
      var number = reader.readInt();
    }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    writer.write("Hello, MiniPack!");
    writer.write(42);
    writer.close();
    read(inStream);
  }
}
