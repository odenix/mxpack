/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.odenix.mxpack.core.MessageReader;
import org.odenix.mxpack.core.MessageWriter;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.assertj.core.api.Assertions.assertThat;

class WriteToFile extends Example {
  // -8<- [start:snippet]
  void write(Path file) throws IOException {
    var channel = FileChannel.open(file, WRITE, CREATE); //(1)
    try (var writer = MessageWriter.of(channel)) {
      writer.write("Hello, MxPack!");
      writer.write(42);
    }
  }
  // -8<- [end:snippet]

  @Test
  void test(@TempDir Path tempDir) throws IOException {
    var file = tempDir.resolve("data.bin");
    write(file);
    var reader = MessageReader.of(FileChannel.open(file));
    assertThat(reader.readString()).isEqualTo("Hello, MxPack!");
    assertThat(reader.readInt()).isEqualTo(42);
  }
}
