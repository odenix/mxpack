/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import org.translatenix.minipack.internal.Utf8StringEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface StringEncoder<T> {
  void write(T string, ByteBuffer writeBuffer, MessageSink sink) throws IOException;

  static StringEncoder<CharSequence> withSizeLimit(int maxBytes) {
    return new Utf8StringEncoder(maxBytes);
  }
}
