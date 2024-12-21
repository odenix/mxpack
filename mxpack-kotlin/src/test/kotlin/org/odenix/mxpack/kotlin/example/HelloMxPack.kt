/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.kotlin.*
import java.nio.channels.FileChannel
import java.nio.file.*
import java.nio.file.StandardOpenOption.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class HelloMxPack : Example() {
  @Suppress("UNUSED_VARIABLE")
  fun run(path: Path) {
    // -8<- [start:snippet]
    val out = FileChannel.open(path, WRITE, CREATE)
    MessageWriter(out).use { writer -> //(1)
      writer.write("Hello, MxPack!")       //(2)
      writer.write(42)                     //(3)
    }

    val ch = FileChannel.open(path)
    MessageReader(ch).use { reader ->  //(4)
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
