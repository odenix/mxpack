/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

/** Indicates an error writing a MessagePack value. */
public class WriterException extends RuntimeException {
  public WriterException(String message) {
    super(message);
  }

  public WriterException(String message, Throwable cause) {
    super(message, cause);
  }
}
