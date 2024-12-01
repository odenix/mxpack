/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import org.odenix.mxpack.core.internal.MessageOutputImpl;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;

/// A container for the in-memory output produced by a [MessageWriter].
///
/// To create a [MessageOutput], call one of its `ofXxx` methods.
/// To pass the [MessageOutput] to a message writer, call [MessageWriter#of].
/// To obtain the produced output, call [MessageOutput#get()] after the message writer has been closed.
///
/// Producing in-memory output requires buffer copying.
/// For best performance, use a message writer that writes to
/// a [java.nio.channels.WritableByteChannel] or [java.io.OutputStream].
///
/// @param <T> the type of output produced
public sealed interface MessageOutput<T> permits MessageOutputImpl, MessageOutput.Buffer {
  /// A container for the [LeasedByteBuffer] produced by a [MessageWriter].
  sealed interface Buffer extends MessageOutput<LeasedByteBuffer> permits MessageOutputImpl.BufferImpl {
    /// A builder of [MessageOutput.Buffer] options.
    sealed interface OptionBuilder permits MessageOutputImpl.BufferImpl.OptionBuilderImpl {
      /// Sets the initial capacity, in bytes, of the byte buffer to be produced.
      ///
      /// Default: 1024
      ///
      /// @param capacity the initial capacity, in bytes, of the byte buffer to be produced
      /// @return this builder
      OptionBuilder initialCapacity(int capacity);
    }
  }

  /// Returns a new [MessageOutput] for a [LeasedByteBuffer].
  ///
  /// @return a new [MessageOutput] for a [LeasedByteBuffer]
  static Buffer ofBuffer() {
    return MessageOutputImpl.BufferImpl.of(options -> {});
  }

  /// Returns a new [MessageOutput] for a [LeasedByteBuffer] with the given options.
  ///
  /// @param optionHandler a handler that receives a [Buffer.OptionBuilder]
  /// @return a new [MessageOutput] for a [LeasedByteBuffer] with the given options
  static Buffer ofBuffer(Consumer<Buffer.OptionBuilder> optionHandler) {
    return MessageOutputImpl.BufferImpl.of(optionHandler);
  }

  /// Returns the output produced by a [MessageWriter].
  ///
  /// @return the output produced by a [MessageWriter]
  /// @throws IllegalStateException if this method is called before the message writer has been closed
  T get();
}
