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
public abstract class WriteValues {
  BufferAllocator allocator;
  BufferAllocator.PooledByteBuffer pooledBuffer;
  ByteBuffer buffer;
  MessageWriter writer;
  ArrayBufferOutput bufferOutput;
  MessagePacker packer;

  abstract void generate256Values();

  @CompilerControl(CompilerControl.Mode.INLINE)
  abstract void writeValue(int index) throws IOException;

  @CompilerControl(CompilerControl.Mode.INLINE)
  abstract void writeValueMp(int index) throws IOException;

  @Setup
  public void setUp() {
    allocator = BufferAllocator.ofUnpooled();
    pooledBuffer = allocator.getByteBuffer(1024 * 16);
    buffer = pooledBuffer.value();
    var sink = MessageSink.ofDiscarding(options -> options.allocator(allocator), pooledBuffer);
    writer = MessageWriter.of(sink);
    bufferOutput = new ArrayBufferOutput(1024 * 16);
    packer = MessagePack.newDefaultPacker(bufferOutput);
    generate256Values();
  }

  @TearDown
  public void tearDown() {
    pooledBuffer.close();
    allocator.close();
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
