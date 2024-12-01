/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import org.junit.jupiter.api.Test;
import org.odenix.mxpack.core.MessageReader;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
class UseDefaultAllocator extends Example {
  class Example {
    // -8<- [start:snippet]
    void read(ReadableByteChannel channel) throws IOException {
      try (var reader = MessageReader.of(channel)) { //(1)
        // read some values
      }
    }
    // -8<- [end:snippet]
  }

  @Test
  void test() throws IOException {
    var example = new Example();
    example.read(inChannel);
  }
}
