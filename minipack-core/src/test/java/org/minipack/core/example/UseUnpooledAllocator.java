/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.example;

import org.junit.jupiter.api.Test;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageWriter;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

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
