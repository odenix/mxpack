/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.benchmark.misc;

import org.odenix.mxpack.core.BufferAllocator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

@SuppressWarnings("unused")
public class PooledAllocator {
  @State(Scope.Benchmark)
  public static class AllocatorState {
    final BufferAllocator allocator = BufferAllocator.ofPooled();
  }

  @Benchmark
  @Threads(1)
  public void run1(AllocatorState state) {
    state.allocator.getByteBuffer(16).close();
  }

  @Benchmark
  @Threads(4)
  public void run4(AllocatorState state) {
    state.allocator.getByteBuffer(16).close();
  }

  @Benchmark
  @Threads(8)
  public void run8(AllocatorState state) {
    state.allocator.getByteBuffer(16).close();
  }
}
