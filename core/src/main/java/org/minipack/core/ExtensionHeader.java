/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

/**
 * The header of a MessagePack extension value.
 *
 * @param length the byte length of the extension value's payload
 * @param type the numeric identifier of the extension type
 */
public record ExtensionHeader(int length, byte type) {
  public static final byte TIMESTAMP_TYPE = -1;

  public boolean isTimestamp() {
    return type == TIMESTAMP_TYPE;
  }
}
