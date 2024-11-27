/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
}
