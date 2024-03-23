/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.nio.ByteBuffer;

public interface StringEncoder {
  int encode(String string, int start, ByteBuffer buffer);

  String decode(ByteBuffer buffer);
}
