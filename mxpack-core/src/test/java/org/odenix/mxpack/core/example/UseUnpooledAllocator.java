/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import org.junit.jupiter.api.Test;
import org.odenix.mxpack.core.BufferAllocator;
import org.odenix.mxpack.core.MessageReader;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
class UseUnpooledAllocator extends Example {
  // -8<- [start:snippet]
  class Example {
    final BufferAllocator allocator =
        BufferAllocator.ofUnpooled( options -> options //(1)
            .maxByteBufferCapacity(Integer.MAX_VALUE)  //(2)
            .maxCharBufferCapacity(Integer.MAX_VALUE)
        );

    void read(ReadableByteChannel channel) throws IOException {
      try (var reader = MessageReader.of(
          channel,
          options -> options.allocator(allocator)) //(3)
      ) { /* read some values */ }
    }

    void close() {
      allocator.close(); //(4)
    }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    var example = new Example();
    example.read(inChannel);
    example.close();
  }
}
