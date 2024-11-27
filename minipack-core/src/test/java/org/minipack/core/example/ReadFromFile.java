/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageWriter;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@SuppressWarnings("unused")
class ReadFromFile extends Example {
  // -8<- [start:snippet]
  void read(Path file) throws IOException {
    var channel = FileChannel.open(file); //(1)
    try (var reader = MessageReader.of(channel)) {
      var string = reader.readString();
      var number = reader.readInt();
    }
  }
  // -8<- [end:snippet]

  @Test
  void test(@TempDir Path tempDir) throws IOException {
    var file = tempDir.resolve("data.bin");
    var writer = MessageWriter.of(FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE));
    writer.write("Hello, MiniPack!");
    writer.write(42);
    writer.close();
    read(file);
  }
}
