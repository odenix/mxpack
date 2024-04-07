/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.bench.write;

import java.io.IOException;
import net.jqwik.api.Arbitraries;

public class WriteInt extends WriteValues {
  int[] values = Arbitraries.integers().array(int[].class).ofSize(256).sample();

  @Override
  void writeValue(int index) throws IOException {
    writer.write(values[index]);
  }

  @Override
  void writeValueMp(int index) throws IOException {
    packer.packInt(values[index]);
  }
}
