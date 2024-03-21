/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import org.jspecify.annotations.Nullable;

// TODO: consider WriterSinkException subclass
public class WriterException extends RuntimeException {
  private WriterException(String message) {
    super(message);
  }

  private WriterException(Throwable cause) {
    super(cause);
  }

  private WriterException(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

  public static WriterException arrayBackedBufferRequired() {
    return new WriterException("This method requires a ByteBuffer backed by an accessible array.");
  }

  public static WriterException ioError(IOException e) {
    return new WriterException("I/O error writing MessagePack message.", e);
  }

  public static WriterException invalidSurrogatePair(int position) {
    return new WriterException(
        "Refusing to write string with invalid surrogate pair at position " + position + ".");
  }

  public static WriterException uncategorized(
      @Nullable String message, @Nullable Throwable throwable) {
    return new WriterException(message, throwable);
  }

  static IllegalStateException sinkRequired() {
    throw new IllegalStateException("MessageWriter.Builder.sink() must be set.");
  }
}
