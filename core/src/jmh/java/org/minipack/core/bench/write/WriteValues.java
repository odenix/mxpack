/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.bench.write;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.minipack.core.BufferAllocator;
import org.minipack.core.MessageWriter;
import org.minipack.core.bench.NullSink;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.buffer.ArrayBufferOutput;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public abstract class WriteValues {
  BufferAllocator allocator;
  ByteBuffer buffer;
  MessageWriter writer;
  ArrayBufferOutput bufferOutput;
  MessagePacker packer;

  abstract void writeValue(int index) throws IOException;

  abstract void writeValueMp(int index) throws IOException;

  @Setup
  public void setUp() {
    allocator = BufferAllocator.pooled().build();
    buffer = allocator.byteBuffer(8 * 1024);
    writer = MessageWriter.builder().sink(new NullSink(buffer, allocator)).build();
    bufferOutput = new ArrayBufferOutput(8 * 1024);
    packer = MessagePack.newDefaultPacker(bufferOutput);
  }

  @Benchmark
  @OperationsPerInvocation(256)
  public void run(Blackhole hole) throws IOException {
    buffer.clear();
    for (var i = 0; i < 256; i++) {
      writeValue(i);
    }
  }

  @Benchmark
  @OperationsPerInvocation(256)
  public void runMp(Blackhole hole) throws IOException {
    bufferOutput.clear();
    packer.reset(bufferOutput);
    for (var i = 0; i < 256; i++) {
      writeValueMp(i);
    }
  }
}