/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public final class SinkOutput<T> implements Supplier<T> {
  private volatile @Nullable T value;

  @Override
  public T get() {
    var result = value;
    if (result == null) {
      throw Exceptions.outputNotAvailable();
    }
    return result;
  }

  void set(T value) {
    this.value = value;
  }
}
