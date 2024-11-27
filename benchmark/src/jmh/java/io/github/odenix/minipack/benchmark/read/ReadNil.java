/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.benchmark.read;

import java.io.IOException;
import io.github.odenix.minipack.core.MessageWriter;
import org.openjdk.jmh.infra.Blackhole;

@SuppressWarnings("unused")
public class ReadNil extends ReadValues {
  @Override
  void write256Values(MessageWriter writer) throws IOException {
    for (int i = 0; i < 256; i++) {
      writer.writeNil();
    }
  }

  @Override
  void readValue(Blackhole hole) throws IOException {
    reader.readNil();
  }

  @Override
  void readValueMp(Blackhole hole) throws IOException {
    unpacker.unpackNil();
  }
}
