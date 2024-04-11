/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.write;

import java.io.IOException;
import java.time.Instant;
import net.jqwik.time.api.DateTimes;

public class WriteTimestamp extends WriteValues {
  Instant[] values;

  @Override
  void generate256Values() {
    values = DateTimes.instants().array(Instant[].class).ofSize(256).sample();
  }

  @Override
  void writeValue(int index) throws IOException {
    writer.write(values[index]);
  }

  @Override
  void writeValueMp(int index) throws IOException {
    packer.packTimestamp(values[index]);
  }
}
