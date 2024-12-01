/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

import java.nio.ByteBuffer;
import org.odenix.mxpack.core.MessageSource;

/// A source provider that reads nothing.
public final class EmptySourceProvider implements MessageSource.Provider {
  @Override
  public int read(ByteBuffer buffer, int minLengthHint) {
    return -1;
  }

  @Override
  public void close() {} // nothing to do
}
