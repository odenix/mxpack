/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.example;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.github.odenix.minipack.core.*;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;

class HelloMiniPack extends Example {
  @SuppressWarnings("unused")
  void run(Path path) throws IOException {
    // -8<- [start:snippet]
    var out = FileChannel.open(path, WRITE, CREATE);
    try (var writer = MessageWriter.of(out)) { //(1)
      writer.write("Hello, MiniPack!");        //(2)
      writer.write(42);                        //(3)
    }

    var in = FileChannel.open(path);
    try (var reader = MessageReader.of(in)) {  //(4)
      var string = reader.readString();        //(5)
      var number = reader.readInt();           //(6)
    }
    // -8<- [end:snippet]
  }

  @Test
  void test(@TempDir Path tempDir) throws IOException {
    var file = tempDir.resolve("data.bin");
    run(file);
  }
}

