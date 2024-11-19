/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

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
