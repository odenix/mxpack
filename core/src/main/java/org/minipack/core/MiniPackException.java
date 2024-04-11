/*
 * Copyright 2024 the MiniPack contributors
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
public abstract class MiniPackException extends RuntimeException {
  public MiniPackException(String message) {
    super(message);
  }

  public MiniPackException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Indicates that a message is too small or large by some metric. */
  public static final class MessageSizeException extends MiniPackException {
    public MessageSizeException(String message) {
      super(message);
    }

    public MessageSizeException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /** Indicates that a message doesn't match the requested type. */
  public static final class TypeMismatchException extends MiniPackException {
    public TypeMismatchException(String message) {
      super(message);
    }

    public TypeMismatchException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static final class InvalidMessageException extends MiniPackException {
    public InvalidMessageException(String message) {
      super(message);
    }

    public InvalidMessageException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static final class SizeLimitExceededException extends MiniPackException {
    public SizeLimitExceededException(String message) {
      super(message);
    }

    public SizeLimitExceededException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
