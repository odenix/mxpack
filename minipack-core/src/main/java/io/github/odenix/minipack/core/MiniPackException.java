/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core;

import java.io.IOException;

/// The root of MiniPack's exception hierarchy.
///
/// MiniPack throws the following exception types:
///
/// * `MiniPackException`
/// * [IOException]
/// * [IllegalArgumentException]
/// * [IllegalStateException]
public abstract class MiniPackException extends RuntimeException {
  /// Indicates that a MessagePack value is incompatible with the requested type.
  public static final class TypeMismatch extends MiniPackException {
    /// Constructs a [TypeMismatch] exception with the given message.
    ///
    /// @param message the exception message
    public TypeMismatch(String message) {
      super(message);
    }
  }

  /// Indicates that a MessagePack value has an invalid header. *
  public static final class InvalidMessageHeader extends MiniPackException {
    /// Constructs an [InvalidMessageHeader] exception with the given message.
    ///
    /// @param message the exception message
    public InvalidMessageHeader(String message) {
      super(message);
    }
  }

  /// Indicates that the size limit of a buffer or other resource has been exceeded. *
  public static final class SizeLimitExceeded extends MiniPackException {
    /// Constructs a [SizeLimitExceeded] exception with the given message.
    ///
    /// @param message the exception message
    public SizeLimitExceeded(String message) {
      super(message);
    }
  }

  /// Constructs a [MiniPackException] exception with the given message.
  ///
  /// @param message the exception message
  protected MiniPackException(String message) {
    super(message);
  }
}
