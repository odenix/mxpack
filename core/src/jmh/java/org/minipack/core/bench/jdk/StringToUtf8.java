/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.bench.jdk;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import net.jqwik.api.Arbitraries;
import org.openjdk.jmh.annotations.*;

@Fork(1)
@State(Scope.Thread)
public class StringToUtf8 {
  @Param({"256"})
  int length;

  @Param({"true", "false"})
  boolean isAscii;

  String value;

  CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
  ByteBuffer byteBuffer = ByteBuffer.allocate(3_000_000);
  char[] charArray = new char[1_000_000];

  @Setup
  public void setUp() {
    value =
        isAscii
            ? Arbitraries.strings().ofLength(length).ascii().sample()
            : Arbitraries.strings().ofLength(length).sample();
  }

  @Benchmark
  public byte[] String_getBytes() {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  @Benchmark
  public ByteBuffer CharsetEncoder_encode() {
    byteBuffer.clear();
    value.getChars(0, value.length(), charArray, 0);
    encoder.encode(CharBuffer.wrap(charArray).limit(value.length()), byteBuffer, true);
    return byteBuffer;
  }

  //  @Benchmark
  //  public ByteBuffer CharsetEncoder_encode_2() {
  //    byteBuffer.clear();
  //    encoder.encode(CharBuffer.wrap(value), byteBuffer, true);
  //    return byteBuffer;
  //  }
}
