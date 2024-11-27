/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.IOException;
import org.minipack.core.internal.CharsetStringDecoder;

/// Reads values from a [MessageSource] and decodes them into values of type [T].
///
/// @param <T> the type of values decoded by this message decoder
@FunctionalInterface
public interface MessageDecoder<T> {
  /// Returns a new message decoder that decodes strings.
  ///
  /// @return a new message decoder that decodes strings
  static MessageDecoder<String> ofStrings() {
    return new CharsetStringDecoder();
  }

  /// Reads the next value from a message source and decodes it into a value of type [T].
  ///
  /// @param source the message source to read from
  /// @return the decoded value
  ///
  /// @throws IOException if an I/O error occurs
  T decode(MessageSource source) throws IOException;
}
