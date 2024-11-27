/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.example;

import org.junit.jupiter.api.Test;
import io.github.odenix.minipack.core.BufferAllocator;
import io.github.odenix.minipack.core.MessageReader;

import java.io.IOException;
import java.nio.channels.*;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
class UsePooledAllocator extends Example {
  // -8<- [start:snippet]
  class Example {
    final BufferAllocator allocator =
        BufferAllocator.ofPooled(options -> options   //(1)
            .maxByteBufferCapacity(Integer.MAX_VALUE) //(2)
            .maxCharBufferCapacity(Integer.MAX_VALUE)
            .maxPooledByteBufferCapacity(1024 * 1024)
            .maxPooledCharBufferCapacity(1024 * 512)
            .maxByteBufferPoolCapacity(1024 * 1024 * 64)
            .maxCharBufferPoolCapacity(1024 * 1024 * 32)
            .preferDirectBuffers(false)
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
