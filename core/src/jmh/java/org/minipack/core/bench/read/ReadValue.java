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
import org.minipack.core.bench.BufferOnlySink;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.ArrayBufferInput;
import org.msgpack.core.buffer.MessageBuffer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@Fork(1)
@State(Scope.Thread)
public abstract class ReadValue {
  BufferAllocator allocator;
  ByteBuffer buffer;
  MessageReader reader;

  MessageBuffer messageBuffer;
  ArrayBufferInput bufferInput;
  MessageUnpacker unpacker;

  abstract void writeValue(MessageWriter writer) throws IOException;

  abstract void readValue(Blackhole hole) throws IOException;

  abstract void readValueMp(Blackhole hole) throws IOException;

  @Setup
  public void setUp() throws IOException {
    allocator = BufferAllocator.unpooled().build();
    buffer = allocator.byteBuffer(8 * 1024);
    var writer = MessageWriter.builder().sink(new BufferOnlySink(buffer, allocator)).build();
    writeValue(writer);
    reader = MessageReader.builder().source(buffer, allocator).build();
    messageBuffer = MessageBuffer.wrap(buffer.array());
    bufferInput = new ArrayBufferInput(messageBuffer);
    unpacker = MessagePack.newDefaultUnpacker(bufferInput);
  }

  @Benchmark
  public void run(Blackhole hole) throws IOException {
    buffer.clear();
    readValue(hole);
  }

  @Benchmark
  public void runMp(Blackhole hole) throws IOException {
    bufferInput.reset(messageBuffer);
    unpacker.reset(bufferInput);
    readValueMp(hole);
  }
}
