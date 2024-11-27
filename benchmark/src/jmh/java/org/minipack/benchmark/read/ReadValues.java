/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.benchmark.read;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.minipack.core.*;
import org.minipack.core.MessageWriter;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.ArrayBufferInput;
import org.msgpack.core.buffer.MessageBuffer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@SuppressWarnings("unused")
public abstract class ReadValues {
  private BufferAllocator allocator;
  private ByteBuffer buffer;
  private MessageBuffer messageBuffer;
  private ArrayBufferInput bufferInput;
  MessageReader reader;
  MessageUnpacker unpacker;

  abstract void write256Values(MessageWriter writer) throws IOException;

  @CompilerControl(CompilerControl.Mode.INLINE)
  abstract void readValue(Blackhole hole) throws IOException;

  @CompilerControl(CompilerControl.Mode.INLINE)
  abstract void readValueMp(Blackhole hole) throws IOException;

  @Setup
  public void setUp() throws IOException {
    allocator = BufferAllocator.ofUnpooled();
    buffer = allocator.getByteBuffer(1024 * 16).get();
    var writer = MessageWriter.ofDiscarding(buffer);
    write256Values(writer);
    buffer.flip();
    reader = MessageReader.of(buffer, options -> options.allocator(allocator));

    messageBuffer = MessageBuffer.wrap(buffer.array());
    bufferInput = new ArrayBufferInput(messageBuffer);
    unpacker = MessagePack.newDefaultUnpacker(bufferInput);
  }

  @TearDown
  public void tearDown() {
    allocator.close();
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
