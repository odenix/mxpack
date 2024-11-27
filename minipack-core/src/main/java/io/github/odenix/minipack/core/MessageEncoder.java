/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core;

import java.io.IOException;
import io.github.odenix.minipack.core.internal.CharsetStringEncoder;

/// Encodes values of type [T] and writes them to a [MessageSink].
///
/// @param <T> the type of values to encode
@FunctionalInterface
public interface MessageEncoder<T> {
  /// Returns a new message encoder that encodes strings.
  ///
  /// @return a new message encoder that encodes strings
  static MessageEncoder<CharSequence> ofStrings() {
    return new CharsetStringEncoder();
  }

  /// Encodes a value and writes it to a message sink.
  ///
  /// @param value the value to encode
  /// @param sink  the message sink to write to
  /// @throws IOException if an I/O error occurs
  void encode(T value, MessageSink sink) throws IOException;
}
