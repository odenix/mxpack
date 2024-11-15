/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.util.function.Consumer;
import org.minipack.java.internal.CharsetStringDecoder;
import org.minipack.java.internal.IdentifierDecoder;

@FunctionalInterface
public interface MessageDecoder<T> {
  static MessageDecoder<String> ofStrings() {
    return new CharsetStringDecoder();
  }

  static MessageDecoder<String> ofStrings(Consumer<StringOptions> consumer) {
    return new CharsetStringDecoder(consumer);
  }

  /**
   * The returned decoder is thread-safe and can be safely shared between multiple message readers.
   */
  static MessageDecoder<String> ofIdentifiers() {
    return new IdentifierDecoder();
  }

  /**
   * The returned decoder is thread-safe and can be safely shared between multiple message readers.
   */
  static MessageDecoder<String> ofIdentifiers(Consumer<IdentifierOptions> options) {
    return new IdentifierDecoder(options);
  }

  interface StringOptions {
    StringOptions charsetDecoder(CharsetDecoder decoder);
  }

  interface IdentifierOptions {
    IdentifierOptions charsetDecoder(CharsetDecoder decoder);

    IdentifierOptions maxCacheSize(int size);
  }

  T decode(MessageSource source, MessageReader reader) throws IOException;
}
