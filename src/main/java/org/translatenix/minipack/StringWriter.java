/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface StringWriter<T> {
  void write(T string, ByteBuffer writeBuffer, MessageSink sink) throws IOException;
}
