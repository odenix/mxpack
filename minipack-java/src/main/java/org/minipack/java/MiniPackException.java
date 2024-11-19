/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

/// The root of MiniPack's exception hierarchy.
///
/// MiniPack code throws the following exception types:
///
///     - `MiniPackException` (including subclasses)
///   - [java.io.IOException] (including subclasses)
///   - [IllegalArgumentException]
///   - [IllegalStateException]
///
public abstract class MiniPackException extends RuntimeException {
  public MiniPackException(String message) {
    super(message);
  }

  public MiniPackException(String message, Throwable cause) {
    super(message, cause);
  }

  /// Indicates that a MessagePack value is incompatible with the requested type.
  public static final class TypeMismatchException extends MiniPackException {
    public TypeMismatchException(String message) {
      super(message);
    }

    public TypeMismatchException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /// Indicates that a MessagePack value has an invalid header. *
  public static final class InvalidMessageHeaderException extends MiniPackException {
    public InvalidMessageHeaderException(String message) {
      super(message);
    }

    public InvalidMessageHeaderException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /// Indicates that the size limit for a resource, such as a buffer, has been exceeded. *
  public static final class SizeLimitExceededException extends MiniPackException {
    public SizeLimitExceededException(String message) {
      super(message);
    }

    public SizeLimitExceededException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /// Indicates that a MessagePack or Java string has an invalid encoding. *
  public static final class InvalidStringEncodingException extends MiniPackException {
    public InvalidStringEncodingException(String message) {
      super(message);
    }

    public InvalidStringEncodingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
