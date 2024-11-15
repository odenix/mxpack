/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.benchmark.write;

import java.io.IOException;
import net.jqwik.api.Arbitraries;

public class WriteShort extends WriteValues {
  short[] values;

  @Override
  void generate256Values() {
    values = Arbitraries.shorts().array(short[].class).ofSize(256).sample();
  }

  @Override
  void writeValue(int index) throws IOException {
    writer.write(values[index]);
  }

  @Override
  void writeValueMp(int index) throws IOException {
    packer.packShort(values[index]);
  }
}
