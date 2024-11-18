/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.minipack.java.MessageSink;

public final class ErrorSinkProvider implements MessageSink.Provider {
  @Override
  public void write(ByteBuffer buffer) throws IOException {
    throw Exceptions.fixedByteBufferOverflow();
  }

  @Override
  public void write(ByteBuffer... buffers) throws IOException {
    throw Exceptions.fixedByteBufferOverflow();
  }

  @Override
  public void flush() throws IOException {} // nothing to do

  @Override
  public void close() throws IOException {} // nothing to do
}
