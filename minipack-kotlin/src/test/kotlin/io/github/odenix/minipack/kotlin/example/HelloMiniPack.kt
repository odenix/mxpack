/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.kotlin.example

import io.github.odenix.minipack.kotlin.*
import java.nio.channels.FileChannel
import java.nio.file.*
import java.nio.file.StandardOpenOption.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class HelloMiniPack : Example() {
  @Suppress("UNUSED_VARIABLE")
  fun run(path: Path) {
    // -8<- [start:snippet]
    val out = FileChannel.open(path, WRITE, CREATE)
    MessageWriters.of(out).use { writer -> //(1)
      writer.write("Hello, MiniPack!")     //(2)
      writer.write(42)                     //(3)
    }

    val ch = FileChannel.open(path)
    MessageReaders.of(ch).use { reader ->  //(4)
      val string = reader.readString()     //(5)
      val number = reader.readInt()        //(6)
    }
    // -8<- [end:snippet]
  }

  @Test
  fun test(@TempDir tempDir: Path) {
    val file = tempDir.resolve("data.bin")
    run(file)
  }
}
