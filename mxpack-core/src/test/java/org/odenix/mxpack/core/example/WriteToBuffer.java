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

import static org.assertj.core.api.Assertions.assertThat;

class WriteToBuffer extends Example {
  // -8<- [start:snippet]
  LeasedByteBuffer write() throws IOException {
    var output = MessageOutput.ofBuffer();
    try (var writer = MessageWriter.of(output)) {
      writer.write("Hello, MxPack!");
      writer.write(42);
    }
    return output.get();
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    var buffer = write();
    var reader = MessageReader.of(buffer);
    assertThat(reader.readString()).isEqualTo("Hello, MxPack!");
    assertThat(reader.readInt()).isEqualTo(42);
  }
}
