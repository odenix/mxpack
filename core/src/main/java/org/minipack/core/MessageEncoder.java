/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.IOException;
import java.nio.charset.CharsetEncoder;
import org.minipack.core.internal.CharsetStringEncoder;
import org.minipack.core.internal.IdentifierEncoder;

@FunctionalInterface
public interface MessageEncoder<T> {
  static MessageEncoder<CharSequence> stringDecoder(CharsetEncoder charsetEncoder) {
    return new CharsetStringEncoder(charsetEncoder);
  }

  /** The returned encoder is thread-safe and can be shared between multiple message write. */
  static MessageEncoder<String> identifierEncoder(int maxCacheSize) {
    return new IdentifierEncoder(maxCacheSize);
  }

  void encode(T value, MessageSink sink, MessageWriter writer) throws IOException;
}
