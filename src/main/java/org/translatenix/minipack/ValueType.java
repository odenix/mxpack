/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

/**
 * The <a href="https://github.com/msgpack/msgpack/blob/master/spec.md#type-system">type</a> of a
 * MessagePack message.
 */
public enum ValueType {
  NIL,
  BOOLEAN,
  INTEGER,
  FLOAT,
  STRING,
  BINARY,
  ARRAY,
  MAP,
  EXTENSION
}
