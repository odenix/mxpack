/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.benchmark.read;

import java.io.IOException;
import net.jqwik.api.Arbitraries;
import io.github.odenix.minipack.core.MessageWriter;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

@SuppressWarnings("unused")
public class ReadString extends ReadValue {
  @Param({"10", "100", "1000"})
  int length;

  @Override
  void writeValue(MessageWriter writer) throws IOException {
    var value = Arbitraries.strings().ofLength(length).sample();
    writer.write(value);
  }

  @Override
  void readValue(Blackhole hole) throws IOException {
    hole.consume(reader.readString());
  }

  @Override
  void readValueMp(Blackhole hole) throws IOException {
    hole.consume(unpacker.unpackString());
  }
}
