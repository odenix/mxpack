/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

/** Indicates an error reading a MessagePack value. */
public class ReaderException extends RuntimeException {
  ReaderException(String message) {
    super(message);
  }

  ReaderException(String message, Throwable cause) {
    super(message, cause);
  }
}
