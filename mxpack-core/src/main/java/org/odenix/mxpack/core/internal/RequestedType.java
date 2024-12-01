/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

/// The type requested when reading a MessagePack value.
public enum RequestedType {
  NIL,
  BOOLEAN,
  BYTE,
  UBYTE,
  SHORT,
  USHORT,
  INT,
  UINT,
  LONG,
  ULONG,
  FLOAT,
  DOUBLE,
  STRING,
  ARRAY,
  MAP,
  BINARY,
  EXTENSION
}
