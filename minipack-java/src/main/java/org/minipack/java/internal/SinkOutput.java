package org.minipack.java.internal;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

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
