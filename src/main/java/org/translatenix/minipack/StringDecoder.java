/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.translatenix.minipack.internal.Utf8StringDecoder;

public interface StringDecoder<T> {
  T decode(ByteBuffer readBuffer, MessageSource source) throws IOException;

  /**
   * Returns a decoder that decodes MessagePack string values ({@code Str format family, UTF-8
   * payload} to values of type {@code java.lang.String}.
   */
  static StringDecoder<String> defaultDecoder(int maxBytes) {
    return new Utf8StringDecoder(maxBytes);
  }
}
