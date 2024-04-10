/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.write;

import java.io.IOException;
import net.jqwik.api.Arbitraries;

public class WriteLong extends WriteValues {
  long[] values;

  @Override
  void generate256Values() {
    values = Arbitraries.longs().array(long[].class).ofSize(256).sample();
  }

  @Override
  void writeValue(int index) throws IOException {
    writer.write(values[index]);
  }

  @Override
  void writeValueMp(int index) throws IOException {
    packer.packLong(values[index]);
  }
}
