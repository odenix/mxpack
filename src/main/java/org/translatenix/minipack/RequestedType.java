/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

/**
 * The type requested when reading a MessagePack value.
 *
 * @see ValueType
 */
public enum RequestedType {
  NIL,
  /** The Java boolean type. */
  BOOLEAN,
  /** The Java byte type. */
  BYTE,
  /** The Java short type. */
  SHORT,
  /** The Java int type. */
  INT,
  /** The Java long type. */
  LONG,
  /** The Java float type. */
  FLOAT,
  /** The Java double type. */
  DOUBLE,
  /** The {@code java.lang.String} type. */
  STRING,
  ARRAY,
  MAP,
  BINARY,
  EXTENSION
}
