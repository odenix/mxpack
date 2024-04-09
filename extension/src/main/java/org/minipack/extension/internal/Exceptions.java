package org.minipack.extension.internal;

import java.nio.charset.CoderResult;

public final class Exceptions {
  private Exceptions() {}

  public static IllegalStateException identifierCacheSizeExceeded(int maxCacheSize) {
    return new IllegalStateException("TODO");
  }

  public static IllegalStateException unknownIdentifier(int id) {
    return new IllegalStateException("TODO");
  }

  public static IllegalStateException stringTooLarge(int length, int maxStringSize) {
    return new IllegalStateException("TODO");
  }

  public static IllegalStateException typeMismatch(byte type) {
    return new IllegalStateException("TODO");
  }

  public static IllegalStateException codingError(CoderResult result) {
    return new IllegalStateException("TODO");
  }

  public static IllegalStateException malformedSurrogate(int i) {
    return new IllegalStateException("TODO");
  }
}
