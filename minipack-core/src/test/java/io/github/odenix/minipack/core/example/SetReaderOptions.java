/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.example;

import org.junit.jupiter.api.Test;
import io.github.odenix.minipack.core.BufferAllocator;
import io.github.odenix.minipack.core.MessageDecoder;
import io.github.odenix.minipack.core.MessageReader;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

@SuppressWarnings("unused")
class SetReaderOptions extends Example {
  // -8<- [start:snippet]
  void read(ReadableByteChannel channel) throws IOException {
    try (var reader = MessageReader.of(channel, options -> options
        .allocator(BufferAllocator.ofUnpooled()) //(1)
        .readBufferCapacity(1024 * 8)
        .stringDecoder(MessageDecoder.ofStrings())
        .identifierDecoder(MessageDecoder.ofStrings()))
    ) { /* read some values */ }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    read(inChannel);
  }
}
