/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.IOException;
import java.nio.charset.CharsetEncoder;
import java.util.function.Consumer;
import org.minipack.java.internal.CharsetStringEncoder;
import org.minipack.java.internal.IdentifierEncoder;

@FunctionalInterface
public interface MessageEncoder<T> {
  static MessageEncoder<CharSequence> ofStrings() {
    return new CharsetStringEncoder();
  }

  static MessageEncoder<CharSequence> ofStrings(Consumer<StringOptions> consumer) {
    return new CharsetStringEncoder(consumer);
  }

  /**
   * The returned encoder is thread-safe and can be safely shared between multiple message writers.
   */
  static MessageEncoder<String> ofIdentifiers() {
    return new IdentifierEncoder();
  }

  /**
   * The returned encoder is thread-safe and can be safely shared between multiple message writers.
   */
  static MessageEncoder<String> ofIdentifiers(Consumer<IdentifierOptions> consumer) {
    return new IdentifierEncoder(consumer);
  }

  interface StringOptions {
    StringOptions charsetEncoder(CharsetEncoder encoder);
  }

  interface IdentifierOptions {
    IdentifierOptions charsetEncoder(CharsetEncoder encoder);

    IdentifierOptions maxCacheSize(int size);
  }

  void encode(T value, MessageSink sink, MessageWriter writer) throws IOException;
}
