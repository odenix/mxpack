/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.benchmark.write;

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
