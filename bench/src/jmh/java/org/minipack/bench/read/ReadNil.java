/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.read;

import java.io.IOException;
import org.minipack.core.MessageWriter;
import org.openjdk.jmh.infra.Blackhole;

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
