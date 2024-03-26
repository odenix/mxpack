/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

/** Classes related to MessagePack extension types. */
public final class ExtensionType {
  private ExtensionType() {}

  /**
   * The header of a MessagePack extension value.
   *
   * @param length the byte length of the extension value's payload
   * @param type the numeric identifier of the extension type
   */
  public record Header(int length, byte type) {}
}
