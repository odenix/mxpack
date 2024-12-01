/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

/// The <a href="https://github.com/msgpack/msgpack/blob/master/spec.md#type-system">type</a> of a
/// MessagePack value.
public enum MessageType {
  /// A nil (null) value.
  NIL,
  /// A boolean value.
  BOOLEAN,
  /// An integer value.
  INTEGER,
  /// A floating point value.
  FLOAT,
  /// A string value.
  STRING,
  /// A binary value.
  BINARY,
  /// An array value.
  ARRAY,
  /// A map value.
  MAP,
  /// An extension value.
  EXTENSION
}
