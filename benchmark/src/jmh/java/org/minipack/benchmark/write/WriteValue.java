/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.benchmark.write;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.minipack.java.BufferAllocator;
import org.minipack.java.MessageSink;
import org.minipack.java.MessageWriter;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.buffer.ArrayBufferOutput;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public abstract class WriteValue {
  BufferAllocator allocator;
  ByteBuffer buffer;
  MessageWriter writer;
  ArrayBufferOutput bufferOutput;
  MessagePacker packer;

  abstract void generateValue();

  @CompilerControl(CompilerControl.Mode.INLINE)
  abstract void writeValue() throws IOException;

  @CompilerControl(CompilerControl.Mode.INLINE)
  abstract void writeValueMp() throws IOException;

  @Setup
  public void setUp() {
    allocator = BufferAllocator.ofUnpooled();
    buffer = allocator.newByteBuffer(1024 * 16);
    var sink = MessageSink.of(buffer, options -> options.allocator(allocator));
    writer = MessageWriter.of(sink);
    bufferOutput = new ArrayBufferOutput(1024 * 16);
    packer = MessagePack.newDefaultPacker(bufferOutput);
    generateValue();
  }

  @Benchmark
  public void run(Blackhole hole) throws IOException {
    buffer.clear();
    writeValue();
  }

  @Benchmark
  public void runMp(Blackhole hole) throws IOException {
    bufferOutput.clear();
    packer.reset(bufferOutput);
    writeValueMp();
  }
}
