/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.benchmark.read;

import java.io.IOException;
import net.jqwik.time.api.DateTimes;
import io.github.odenix.minipack.core.MessageWriter;
import org.openjdk.jmh.infra.Blackhole;

@SuppressWarnings("unused")
public class ReadTimestamp extends ReadValues {
  @Override
  void write256Values(MessageWriter writer) throws IOException {
    var values = DateTimes.instants().list().ofSize(256).sample();
    for (var v : values) writer.write(v);
  }

  @Override
  void readValue(Blackhole hole) throws IOException {
    hole.consume(reader.readTimestamp());
  }

  @Override
  void readValueMp(Blackhole hole) throws IOException {
    hole.consume(unpacker.unpackTimestamp());
  }
}
