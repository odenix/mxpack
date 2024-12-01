/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.kotlin.MessageReaders
import org.odenix.mxpack.kotlin.MessageWriters
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class WriteToFile : Example() {
  // -8<- [start:snippet]
  fun write(file: Path) {
    val channel = FileChannel.open(file, WRITE, CREATE) //(1)
    MessageWriters.of(channel).use { writer ->
      writer.write("Hello, MxPack!")
      writer.write(42)
    }
  }
  // -8<- [end:snippet]

  @Test
  fun test(@TempDir dir: Path) {
    var file = dir.resolve("data.bin")
    write(file)
    var reader = MessageReaders.of(FileChannel.open(file))
    assertThat(reader.readString()).isEqualTo("Hello, MxPack!")
    assertThat(reader.readInt()).isEqualTo(42)
    reader.close()
  }
}
