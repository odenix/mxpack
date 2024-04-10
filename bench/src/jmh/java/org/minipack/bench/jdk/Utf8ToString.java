/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.jdk;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import net.jqwik.api.Arbitraries;
import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
public class Utf8ToString {
  @Param({"20"})
  int length;

  @Param({"true", "false"})
  boolean isAscii;

  byte[] value;

  CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
  CharBuffer charBuffer;
  ByteBuffer byteBuffer;

  @Setup
  public void setUp() {
    var string =
        isAscii
            ? Arbitraries.strings().ofLength(length).ascii().sample()
            : Arbitraries.strings().ofLength(length).sample();
    value = string.getBytes(StandardCharsets.UTF_8);
    charBuffer = CharBuffer.allocate(length);
    byteBuffer = ByteBuffer.wrap(value);
  }

  @Benchmark
  public String new_String() {
    return new String(value, StandardCharsets.UTF_8);
  }

  @Benchmark
  public String CharsetDecoder_decode() {
    charBuffer.position(0);
    byteBuffer.position(0);
    decoder.decode(byteBuffer, charBuffer, true);
    return new String(charBuffer.array());
  }
}
