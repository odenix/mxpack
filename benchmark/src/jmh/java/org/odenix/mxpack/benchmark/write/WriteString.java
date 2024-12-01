/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.benchmark.write;

import java.io.IOException;
import net.jqwik.api.Arbitraries;
import org.openjdk.jmh.annotations.Param;

@SuppressWarnings("unused")
public class WriteString extends WriteValue {
  @Param({"10", "100", "1000"})
  int length;

  String value;

  @Override
  void generateValue() {
    value = Arbitraries.strings().ofLength(length).sample();
  }

  @Override
  void writeValue() throws IOException {
    writer.write(value);
  }

  @Override
  void writeValueMp() throws IOException {
    packer.packString(value);
  }
}
