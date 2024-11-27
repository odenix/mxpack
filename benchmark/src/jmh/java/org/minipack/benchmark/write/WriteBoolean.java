/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.benchmark.write;

import java.io.IOException;
import java.util.Random;

@SuppressWarnings("unused")
public class WriteBoolean extends WriteValues {
  boolean[] values;

  @Override
  void generate256Values() {
    var random = new Random();
    values = new boolean[256];
    for (int i = 0; i < 256; i++) {
      values[i] = random.nextBoolean();
    }
  }

  @Override
  void writeValue(int index) throws IOException {
    writer.write(values[index]);
  }

  @Override
  void writeValueMp(int index) throws IOException {
    packer.packBoolean(values[index]);
  }
}
