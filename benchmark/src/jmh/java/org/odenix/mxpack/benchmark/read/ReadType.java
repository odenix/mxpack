/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.benchmark.read;

import java.io.IOException;
import net.jqwik.api.Arbitraries;
import org.odenix.mxpack.core.MessageWriter;
import org.odenix.mxpack.core.internal.MessageFormat;
import org.openjdk.jmh.infra.Blackhole;

@SuppressWarnings("unused")
public class ReadType extends ReadValues {
  @Override
  void write256Values(MessageWriter writer) throws IOException {
    var values =
        Arbitraries.bytes()
            .filter(b -> b != MessageFormat.NEVER_USED)
            .array(byte[].class)
            .ofSize(256)
            .sample();
    for (var v : values) writer.write(v);
  }

  @Override
  void readValue(Blackhole hole) throws IOException {
    hole.consume(reader.nextType());
  }

  @Override
  void readValueMp(Blackhole hole) throws IOException {
    hole.consume(unpacker.getNextFormat().getValueType());
  }
}
