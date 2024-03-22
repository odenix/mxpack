/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

/**
 * The Java type that a MessagePack value is converted to.
 *
 * @see ValueType
 */
public enum JavaType {
  /** The Java void type. */
  VOID,
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
  /** The Java array type. */
  ARRAY,
  /** The {@code java.util.Map} type. */
  MAP
}
