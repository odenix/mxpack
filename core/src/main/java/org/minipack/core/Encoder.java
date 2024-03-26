/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.minipack.core.internal.IdentifierEncoder;
import org.minipack.core.internal.StringEncoder;

public interface Encoder<T> {
  static Encoder<CharSequence> stringEncoder(int maxStringSize) {
    return new StringEncoder(maxStringSize);
  }

  /** The returned encoder is thread-safe and can be shared between multiple message write. */
  static Encoder<String> identifierEncoder(int maxCacheSize) {
    return new IdentifierEncoder(maxCacheSize);
  }

  void encode(T value, ByteBuffer buffer, MessageSink sink) throws IOException;
}
