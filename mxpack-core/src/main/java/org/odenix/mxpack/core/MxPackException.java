/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import java.io.IOException;

/// The root of MxPack's exception hierarchy.
///
/// MxPack throws the following exception types:
///
/// * `MxPackException`
/// * [IOException]
/// * [IllegalArgumentException]
/// * [IllegalStateException]
public abstract class MxPackException extends RuntimeException {
  /// Indicates that a MessagePack value is incompatible with the requested type.
  public static final class TypeMismatch extends MxPackException {
    /// Constructs a [TypeMismatch] exception with the given message.
    ///
    /// @param message the exception message
    public TypeMismatch(String message) {
      super(message);
    }
  }

  /// Indicates that a MessagePack value has an invalid header. *
  public static final class InvalidMessageHeader extends MxPackException {
    /// Constructs an [InvalidMessageHeader] exception with the given message.
    ///
    /// @param message the exception message
    public InvalidMessageHeader(String message) {
      super(message);
    }
  }

  /// Indicates that the size limit of a buffer or other resource has been exceeded. *
  public static final class SizeLimitExceeded extends MxPackException {
    /// Constructs a [SizeLimitExceeded] exception with the given message.
    ///
    /// @param message the exception message
    public SizeLimitExceeded(String message) {
      super(message);
    }
  }

  /// Constructs a [MxPackException] exception with the given message.
  ///
  /// @param message the exception message
  protected MxPackException(String message) {
    super(message);
  }
}
