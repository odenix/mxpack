/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.translatenix.minipack.internal.Utf8StringReader;

public interface StringReader<T> {
  T read(ByteBuffer readBuffer, MessageSource source) throws IOException;

  static StringReader<String> withSizeLimit(int maxBytes) {
    return new Utf8StringReader(maxBytes);
  }
}
