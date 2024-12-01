/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import org.junit.jupiter.api.Test;
import org.odenix.mxpack.core.MessageWriter;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import static org.assertj.core.api.Assertions.assertThat;

class WriteToChannel extends Example {
  // -8<- [start:snippet]
  void write(WritableByteChannel channel) throws IOException {
    try (var writer = MessageWriter.of(channel)) {
      writer.write("Hello, MxPack!");
      writer.write(42);
    }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    write(outChannel);
    assertThat(reader.readString()).isEqualTo("Hello, MxPack!");
    assertThat(reader.readInt()).isEqualTo(42);
  }
}
