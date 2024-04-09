/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.write;

import java.io.IOException;
import net.jqwik.api.Arbitraries;

public class WriteString extends WriteValue {
  String value = Arbitraries.strings().ofLength(256).sample();

  @Override
  void writeValue() throws IOException {
    writer.write(value);
  }

  @Override
  void writeValueMp() throws IOException {
    packer.packString(value);
  }
}
