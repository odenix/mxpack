/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class PackerBenchmark {
  @Benchmark
  public void bench1(Blackhole bh) {
    bh.consume(30);
  }

  @Benchmark
  public void bench2(Blackhole bh) {
    bh.consume(30);
  }
}
