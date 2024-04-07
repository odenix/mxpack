/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import org.minipack.core.internal.CharsetStringDecoder;
import org.minipack.core.internal.IdentifierDecoder;

@FunctionalInterface
public interface MessageDecoder<T> {
  static MessageDecoder<String> stringDecoder(CharsetDecoder charsetDecoder) {
    return new CharsetStringDecoder(charsetDecoder);
  }

  /** The returned decoder is thread-safe and can be shared between multiple message readers. */
  static MessageDecoder<String> identifierDecoder(int maxCacheSize) {
    return new IdentifierDecoder(maxCacheSize);
  }

  T decode(MessageSource source, MessageReader reader) throws IOException;
}
