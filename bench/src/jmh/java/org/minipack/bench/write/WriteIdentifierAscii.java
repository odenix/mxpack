/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.write;

import net.jqwik.api.Arbitraries;

import java.io.IOException;

public class WriteIdentifierAscii extends WriteValues {
  String[] values = Arbitraries.strings().ofMinLength(2).ofMaxLength(20).ascii().array(String[].class).ofSize(256).sample();

  @Override
  void writeValue(int index) throws IOException {
    writer.writeIdentifier(values[index]);
  }

  @Override
  void writeValueMp(int index) throws IOException {
    packer.packString(values[index]);
  }
}
