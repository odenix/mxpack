/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.IOException;
import java.nio.charset.CharsetEncoder;
import org.minipack.core.internal.CharsetStringEncoder;
import org.minipack.core.internal.IdentifierEncoder;

@FunctionalInterface
public interface MessageEncoder<T> {
  static MessageEncoder<CharSequence> stringEncoder(CharsetEncoder charsetEncoder) {
    return new CharsetStringEncoder(charsetEncoder);
  }

  /** The returned encoder is thread-safe and can be shared between multiple message write. */
  static MessageEncoder<String> identifierEncoder(CharsetEncoder charsetEncoder, int maxCacheSize) {
    return new IdentifierEncoder(charsetEncoder, maxCacheSize);
  }

  void encode(T value, MessageSink sink, MessageWriter writer) throws IOException;
}
