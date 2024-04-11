/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.bench.write;

import java.io.IOException;

public class WriteNil extends WriteValues {
  @Override
  void generate256Values() {}

  @Override
  void writeValue(int index) throws IOException {
    writer.writeNil();
  }

  @Override
  void writeValueMp(int index) throws IOException {
    packer.packNil();
  }
}
