/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.util.function.Consumer;
import org.minipack.java.internal.CharsetStringDecoder;

@FunctionalInterface
public interface MessageDecoder<T> {
  static MessageDecoder<String> ofStrings() {
    return new CharsetStringDecoder();
  }

  static MessageDecoder<String> ofStrings(Consumer<StringOptions> consumer) {
    return new CharsetStringDecoder(consumer);
  }

  interface StringOptions {
    StringOptions charsetDecoder(CharsetDecoder decoder);
  }

  T decode(MessageSource source, MessageReader reader) throws IOException;
}
