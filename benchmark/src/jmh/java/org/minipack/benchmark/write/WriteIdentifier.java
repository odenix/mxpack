/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.benchmark.write;

import java.io.IOException;
import net.jqwik.api.Arbitraries;

public class WriteIdentifier extends WriteValues {
  String[] values;

  @Override
  void generate256Values() {
    values =
        Arbitraries.strings()
            .ofMinLength(2)
            .ofMaxLength(20)
            .array(String[].class)
            .ofSize(256)
            .sample();
  }

  @Override
  void writeValue(int index) throws IOException {
    writer.writeIdentifier(values[index]);
  }

  @Override
  void writeValueMp(int index) throws IOException {
    packer.packString(values[index]);
  }
}
