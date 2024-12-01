/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.internal;

import org.jspecify.annotations.Nullable;
import org.odenix.mxpack.core.LeasedByteBuffer;
import org.odenix.mxpack.core.MessageOutput;
import org.odenix.mxpack.core.MessageWriter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/// Implementation of [MessageOutput].
public abstract sealed class MessageOutputImpl<T> implements MessageOutput<T> {
  private final AtomicReference<@Nullable T> output = new AtomicReference<>();

  public final static class BufferImpl extends MessageOutputImpl<LeasedByteBuffer> implements MessageOutput.Buffer {
    final int initialCapacity;

    public final static class OptionBuilderImpl implements Buffer.OptionBuilder {
      private int initialCapacity = 1024;

      @Override
      public OptionBuilderImpl initialCapacity(int capacity) {
        initialCapacity = capacity;
        return this;
      }
    }

    public static BufferImpl of(Consumer<Buffer.OptionBuilder> optionHandler) {
      var options = new OptionBuilderImpl();
      optionHandler.accept(options);
      return new BufferImpl(options.initialCapacity);
    }

    private BufferImpl(int initialCapacity) {
      this.initialCapacity = initialCapacity;
    }
  }

  @Override
  public final T get() {
    var result = output.get();
    if (result == null) {
      throw Exceptions.outputNotAvailable();
    }
    return result;
  }

  final void set(T output) {
    Objects.requireNonNull(output);
    if (this.output.getAndSet(output) != null) {
      throw Exceptions.outputAlreadySet();
    }
  }
}
