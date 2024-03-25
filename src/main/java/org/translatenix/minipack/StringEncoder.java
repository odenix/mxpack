/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.translatenix.minipack.internal.Utf8StringEncoder;

public interface StringEncoder<T> {
  void encode(T string, ByteBuffer writeBuffer, MessageSink sink) throws IOException;

  /**
   * Returns an encoder that encodes values of type {@code java.lang.CharSequence} to MessagePack
   * string values ({@code Str} format family, UTF-8 payload).
   */
  static StringEncoder<CharSequence> defaultEncoder(int maxBytes) {
    return new Utf8StringEncoder(maxBytes);
  }
}
