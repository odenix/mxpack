/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import org.junit.jupiter.api.Test;
import org.odenix.mxpack.core.LeasedByteBuffer;
import org.odenix.mxpack.core.MessageOutput;
import org.odenix.mxpack.core.MessageReader;
import org.odenix.mxpack.core.MessageWriter;

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
    var output = MessageOutput.ofBuffer();
    var writer = MessageWriter.of(output);
    writer.write("Hello, MxPack!");
    writer.write(42);
    writer.close();
    read(output.get());
  }
}
