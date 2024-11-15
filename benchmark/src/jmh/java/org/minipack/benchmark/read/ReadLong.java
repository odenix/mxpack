/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.benchmark.read;

import java.io.IOException;
import net.jqwik.api.Arbitraries;
import org.minipack.java.MessageWriter;
import org.openjdk.jmh.infra.Blackhole;

public class ReadLong extends ReadValues {
  @Override
  void write256Values(MessageWriter writer) throws IOException {
    var values = Arbitraries.longs().list().ofSize(256).sample();
    for (var v : values) writer.write(v);
  }

  @Override
  void readValue(Blackhole hole) throws IOException {
    hole.consume(reader.readLong());
  }

  @Override
  void readValueMp(Blackhole hole) throws IOException {
    hole.consume(unpacker.unpackLong());
  }
}
