/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.translatenix.minipack.internal.Utf8StringEncoder;

/**
 * Encodes string values of type {@code T} and writes them to a message sink.
 *
 * @param <T> the type of a decoded string value
 */
public interface StringEncoder<T> {
  /**
   * Returns an encoder that encodes values of type {@code java.lang.CharSequence} to MessagePack
   * string values ({@code Str} format family, UTF-8 payload).
   */
  static StringEncoder<CharSequence> defaultEncoder(int maxBytes) {
    return new Utf8StringEncoder(maxBytes);
  }

  /**
   * Encodes the given string.
   *
   * <p>Ownership of the given buffer is transferred to the callee until this method returns. Any
   * bytes between the start and current {@linkplain ByteBuffer#position() position} of the given
   * buffer still need to be written to the given sink.
   */
  void encode(T string, ByteBuffer buffer, MessageSink sink) throws IOException;
}
