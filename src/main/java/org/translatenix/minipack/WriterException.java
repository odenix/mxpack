/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;

public class WriterException extends RuntimeException {
  private WriterException(String message) {
    super(message);
  }

  private WriterException(Throwable cause) {
    super(cause);
  }

  private WriterException(String message, Throwable cause) {
    super(message, cause);
  }

  static IllegalStateException sinkRequired() {
    throw new IllegalStateException("MessageWriter.Builder.sink() must be set.");
  }

  static WriterException invalidSurrogatePair(int position) {
    return new WriterException(
        "Refusing to write string with invalid surrogate pair at position " + position + ".");
  }

  static WriterException ioErrorWritingToSink(IOException e) {
    return new WriterException("I/O error writing to message sink.", e);
  }

  static WriterException ioErrorFlushingSink(IOException e) {
    return new WriterException("I/O error flushing message sink.", e);
  }
}
