/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core;

/// The header of an extension value.
///
/// @param length the length, in bytes, of the extension value's payload
/// @param type the numeric identifier of the extension value's type
public record ExtensionHeader(int length, byte type) {
  /// The numeric identifier of extension type `timestamp`.
  public static final byte TIMESTAMP_TYPE = -1;

  /// Returns whether this extension value has type `timestamp`.
  ///
  /// @return whether this extension value has type `timestamp`
  public boolean isTimestamp() {
    return type == TIMESTAMP_TYPE;
  }
}
