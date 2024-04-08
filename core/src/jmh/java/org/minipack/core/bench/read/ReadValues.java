/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.bench.read;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageReader;
import org.minipack.core.MessageWriter;
import org.minipack.core.bench.NullSink;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.ArrayBufferInput;
import org.msgpack.core.buffer.MessageBuffer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@Fork(1)
@State(Scope.Thread)
public abstract class ReadValues {
  BufferAllocator allocator;
  ByteBuffer buffer;
  MessageReader reader;

  MessageBuffer messageBuffer;
  ArrayBufferInput bufferInput;
  MessageUnpacker unpacker;

  abstract void writeValues(MessageWriter writer) throws IOException;

  abstract void readValue(Blackhole hole) throws IOException;

  abstract void readValueMp(Blackhole hole) throws IOException;

  @Setup
  public void setUp() throws IOException {
    allocator = BufferAllocator.unpooled().build();
    buffer = ByteBuffer.allocate(8 * 1024);
    var writer = MessageWriter.builder().sink(new NullSink(buffer, allocator)).build();
    writeValues(writer);
    reader = MessageReader.builder().source(buffer, allocator).build();
    messageBuffer = MessageBuffer.wrap(buffer.array());
    bufferInput = new ArrayBufferInput(messageBuffer);
    unpacker = MessagePack.newDefaultUnpacker(bufferInput);
  }

  @Benchmark
  @OperationsPerInvocation(256)
  public void run(Blackhole hole) throws IOException {
    buffer.clear();
    for (var i = 0; i < 256; i++) {
      readValue(hole);
    }
  }

  @Benchmark
  @OperationsPerInvocation(256)
  public void runMp(Blackhole hole) throws IOException {
    bufferInput.reset(messageBuffer);
    unpacker.reset(bufferInput);
    for (var i = 0; i < 256; i++) {
      readValueMp(hole);
    }
  }
}
