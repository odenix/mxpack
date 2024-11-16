/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.IOException;
import java.nio.charset.CharsetEncoder;
import java.util.function.Consumer;
import org.minipack.java.internal.CharsetStringEncoder;

@FunctionalInterface
public interface MessageEncoder<T> {
  static MessageEncoder<CharSequence> ofStrings() {
    return new CharsetStringEncoder();
  }

  static MessageEncoder<CharSequence> ofStrings(Consumer<StringOptions> consumer) {
    return new CharsetStringEncoder(consumer);
  }

  interface StringOptions {
    StringOptions charsetEncoder(CharsetEncoder encoder);
  }

  void encode(T value, MessageSink sink, MessageWriter writer) throws IOException;
}
