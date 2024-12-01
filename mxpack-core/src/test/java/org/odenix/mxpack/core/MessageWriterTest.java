/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/// @see [WriteTest]
/// @see [RoundtripTest]
class MessageWriterTest {
  @Test
  void ofDiscarding() {
    var writer = MessageWriter.ofDiscarding();
    assertThatCode(() -> {
      for (int i = 0; i < 100; i++) {
        writer.write("the quick brown fox jumps over the lazy dog");
      }
    }).doesNotThrowAnyException();
  }

  @Test
  void ofBufferOutput() throws IOException {
    var output = MessageOutput.ofBuffer(options -> options.initialCapacity(123));
    try (var writer = MessageWriter.of(output)) {
      writer.write(42);
    }
    var buffer = output.get().get();
    assertThat(buffer.capacity()).isEqualTo(123);
    assertThat(buffer.get()).isEqualTo((byte) 42); // fixint
    assertThat(buffer.remaining()).isEqualTo(0);
  }

  @Test
  void writeStringPayload() throws IOException {
    var string = "Hello, MiniPack!";
    var bytes = string.getBytes(StandardCharsets.UTF_8);
    var output = MessageOutput.ofBuffer();
    try (var writer = MessageWriter.of(output)) {
      writer.writeStringHeader(bytes.length);
      writer.writePayload(ByteBuffer.wrap(bytes));
    }
    try (var reader = MessageReader.of(output.get())) {
      assertThat(reader.readString()).isEqualTo(string);
    }
  }
}
