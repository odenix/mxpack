/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.translatenix.minipack.internal.IdentifierEncoder;
import org.translatenix.minipack.internal.StringEncoder;

public interface Encoder<T> {
  static Encoder<CharSequence> defaultStringEncoder(int maxStringSize) {
    return new StringEncoder(maxStringSize);
  }

  static Encoder<String> defaultIdentifierEncoder(int maxCacheSize) {
    return new IdentifierEncoder(maxCacheSize);
  }

  void encode(T value, ByteBuffer buffer, MessageSink sink) throws IOException;
}
