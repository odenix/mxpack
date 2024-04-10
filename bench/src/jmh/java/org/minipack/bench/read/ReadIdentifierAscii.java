/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.read;

import java.io.IOException;
import net.jqwik.api.Arbitraries;
import org.minipack.core.MessageWriter;
import org.openjdk.jmh.infra.Blackhole;

public class ReadIdentifierAscii extends ReadValues {
  @Override
  void write256Values(MessageWriter writer) throws IOException {
    var values =
        Arbitraries.strings().ofMinLength(2).ofMaxLength(20).ascii().list().ofSize(256).sample();
    for (var v : values) writer.write(v);
  }

  @Override
  void readValue(Blackhole hole) throws IOException {
    hole.consume(reader.readIdentifier());
  }

  @Override
  void readValueMp(Blackhole hole) throws IOException {
    hole.consume(unpacker.unpackString());
  }
}
