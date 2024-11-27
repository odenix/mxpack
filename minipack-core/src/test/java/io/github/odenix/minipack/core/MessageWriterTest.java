/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core;

import org.junit.jupiter.api.Test;

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
}
