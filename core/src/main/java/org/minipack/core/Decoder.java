/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.minipack.core.internal.IdentifierDecoder;
import org.minipack.core.internal.StringDecoder;

public interface Decoder<T> {
  static Decoder<String> stringDecoder(int maxStringSize) {
    return new StringDecoder(maxStringSize);
  }

  /** The returned decoder is thread-safe and can be shared between multiple message readers. */
  static Decoder<String> identifierDecoder(int maxCacheSize) {
    return new IdentifierDecoder(maxCacheSize);
  }

  T decode(ByteBuffer buffer, MessageSource source) throws IOException;
}
