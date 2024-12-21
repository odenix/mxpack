/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import org.odenix.mxpack.core.BufferAllocator;
import org.odenix.mxpack.core.MessageEncoder;
import org.odenix.mxpack.core.MessageWriter;
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
        .stringEncoder(MessageEncoder.ofString())
        .identifierEncoder(MessageEncoder.ofString()))
    ) { /* write some values */ }
  }
  // -8<- [end:snippet]

  @Test
  void test() throws IOException {
    write(outChannel);
  }
}
