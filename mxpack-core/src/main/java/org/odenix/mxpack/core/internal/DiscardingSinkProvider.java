/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.odenix.mxpack.core.MessageSink;

/// A message sink provider that discards any bytes written.
public final class DiscardingSinkProvider implements MessageSink.Provider {
  @Override
  public void write(ByteBuffer buffer) throws IOException {} // nothing to do

  @Override
  public void write(ByteBuffer... buffers) throws IOException {} // nothing to do

  @Override
  public void flush() throws IOException {} // nothing to do

  @Override
  public void close() throws IOException {} // nothing to do
}
