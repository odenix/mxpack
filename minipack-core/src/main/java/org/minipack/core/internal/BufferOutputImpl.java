/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import org.jspecify.annotations.Nullable;
import org.minipack.core.LeasedByteBuffer;
import org.minipack.core.MessageWriter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/// Implementation of [MessageWriter.BufferOutput].
public final class BufferOutputImpl implements MessageWriter.BufferOutput {
  final int initialCapacity;
  private final AtomicReference<@Nullable LeasedByteBuffer> buffer = new AtomicReference<>();

  public static final class OptionBuilderImpl implements OptionBuilder {
    private int initialCapacity = 1024;

    @Override
    public OptionBuilder initialCapacity(int capacity) {
      this.initialCapacity = capacity;
      return this;
    }
  }

  public static MessageWriter.BufferOutput of(Consumer<OptionBuilder> optionHandler) {
    var options = new OptionBuilderImpl();
    optionHandler.accept(options);
    return new BufferOutputImpl(options.initialCapacity);
  }

  public BufferOutputImpl(int initialCapacity) {
    this.initialCapacity = initialCapacity;
  }

  @Override
  public LeasedByteBuffer get() {
    var result = buffer.get();
    if (result == null) {
      throw Exceptions.outputNotAvailable();
    }
    return result;
  }

  void set(LeasedByteBuffer buffer) {
    Objects.requireNonNull(buffer);
    if (this.buffer.getAndSet(buffer) != null) {
      throw Exceptions.outputAlreadySet();
    }
  }
}
