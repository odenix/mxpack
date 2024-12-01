/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import org.junit.jupiter.api.Test;
import org.odenix.mxpack.core.MessageWriter;

import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class WriteToStream extends Example {
  // -8<- [start:snippet]
  void write(OutputStream stream) throws IOException {
    try (var writer = MessageWriter.of(stream)) {
      writer.write("Hello, MxPack!");
      writer.write(42);
    }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    write(outStream);
    assertThat(reader.readString()).isEqualTo("Hello, MxPack!");
    assertThat(reader.readInt()).isEqualTo(42);
  }
}
