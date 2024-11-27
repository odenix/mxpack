/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import org.junit.jupiter.api.Test;

import java.io.EOFException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/// @see [ReadTest]
/// @see [RoundtripTest]
class MessageReaderTest {
  @Test
  void ofEmpty() {
    var reader = MessageReader.ofEmpty();
    assertThatExceptionOfType(EOFException.class).isThrownBy(() -> {
      reader.readByte();
    });
  }
}
