/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.benchmark.write;

import java.io.IOException;

@SuppressWarnings("unused")
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