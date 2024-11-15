/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.benchmark;

import java.nio.ByteBuffer;
import org.minipack.java.MessageSink;

public final class BenchmarkSinkProvider implements MessageSink.Provider<Void> {
  @Override
  public void write(ByteBuffer buffer) {}

  @Override
  public void write(ByteBuffer... buffers) {}

  @Override
  public void flush() {}

  @Override
  public void close() {}
}
