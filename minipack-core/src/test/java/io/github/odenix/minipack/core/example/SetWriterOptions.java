/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.example;

import io.github.odenix.minipack.core.BufferAllocator;
import io.github.odenix.minipack.core.MessageEncoder;
import io.github.odenix.minipack.core.MessageWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

@SuppressWarnings("unused")
class SetWriterOptions extends Example {
  // -8<- [start:snippet]
  void write(WritableByteChannel channel) throws IOException {
    try (var writer = MessageWriter.of(channel, options -> options
        .allocator(BufferAllocator.ofUnpooled()) //(1)
        .writeBufferCapacity(1024 * 8)
        .stringEncoder(MessageEncoder.ofStrings())
        .identifierEncoder(MessageEncoder.ofStrings()))
    ) { /* write some values */ }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    write(outChannel);
  }
}
