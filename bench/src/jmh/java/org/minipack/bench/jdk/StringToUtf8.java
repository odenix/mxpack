/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.jdk;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import net.jqwik.api.Arbitraries;
import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
public class StringToUtf8 {
  @Param({"256"})
  int length;

  @Param({"true", "false"})
  boolean isAscii;

  String value;

  CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
  CharBuffer charBuffer;
  ByteBuffer byteBuffer;

  @Setup
  public void setUp() {
    value =
        isAscii
            ? Arbitraries.strings().ofLength(length).ascii().sample()
            : Arbitraries.strings().ofLength(length).sample();
    charBuffer = CharBuffer.allocate(length);
    byteBuffer = ByteBuffer.allocate(length * 3);
  }

  @Benchmark
  public byte[] String_getBytes() {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  @Benchmark
  public ByteBuffer CharsetEncoder_encode() {
    charBuffer.position(0);
    byteBuffer.position(0);
    value.getChars(0, value.length(), charBuffer.array(), 0);
    encoder.encode(charBuffer, byteBuffer, true);
    return byteBuffer;
  }

  // @Benchmark
  public ByteBuffer CharsetEncoder_encode_2() {
    byteBuffer.position(0);
    encoder.encode(CharBuffer.wrap(value), byteBuffer, true);
    return byteBuffer;
  }
}
