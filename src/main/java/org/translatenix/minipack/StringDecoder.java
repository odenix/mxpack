/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.translatenix.minipack.internal.Utf8StringDecoder;

/**
 * Reads string values from a message source and decodes them to values of type {@code T}.
 *
 * @param <T> the type of a decoded string value
 */
public interface StringDecoder<T> {
  /**
   * Returns a decoder that decodes MessagePack string values ({@code Str format family, UTF-8
   * payload} to values of type {@code java.lang.String}.
   */
  static StringDecoder<String> defaultDecoder(int maxBytes) {
    return new Utf8StringDecoder(maxBytes);
  }

  /**
   * Reads a string value from the given buffer and source and returns the decoded value.
   *
   * <p>The {@linkplain ByteBuffer#remaining() remaining} content of the given buffer contains the
   * initial or possibly all bytes of the string value's header and payload. If necessary, more
   * bytes can be read from the given source.
   *
   * <p>Ownership of the given buffer is transferred to the callee until this method returns. If the
   * decoded value is not a string value, or if its size is too large, a {@link ReaderException} is
   * thrown.
   */
  T decode(ByteBuffer buffer, MessageSource source) throws IOException;
}
