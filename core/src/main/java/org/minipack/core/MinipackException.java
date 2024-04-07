/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

/**
 * The root of minipack's exception hierarchy.
 *
 * <p>Minipack throws the following exceptions:
 *
 * <ul>
 *   <li>{@code MinipackException} (including subclasses)
 *   <li>{@link java.io.IOException} (including subclasses)
 *   <li>{@link IllegalArgumentException}
 *   <li>{@link IllegalStateException}
 * </ul>
 */
public abstract class MinipackException extends RuntimeException {
  public MinipackException(String message) {
    super(message);
  }

  public MinipackException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Indicates that a message is too small or large by some metric. */
  public static final class MessageSizeException extends MinipackException {
    public MessageSizeException(String message) {
      super(message);
    }

    public MessageSizeException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /** Indicates that a message doesn't match the requested type. */
  public static final class TypeMismatchException extends MinipackException {
    public TypeMismatchException(String message) {
      super(message);
    }

    public TypeMismatchException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static final class InvalidMessageException extends MinipackException {
    public InvalidMessageException(String message) {
      super(message);
    }

    public InvalidMessageException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static final class SizeLimitExceededException extends MinipackException {
    public SizeLimitExceededException(String message) {
      super(message);
    }

    public SizeLimitExceededException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
