/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.util.concurrent.atomic.AtomicBoolean;

/// Common base class for closeable resources.
public abstract class AbstractCloseable {
  // Use atomic variable because some subclasses need to ensure
  // that their close() method is executed at most once.
  private final AtomicBoolean isClosed = new AtomicBoolean();

  protected final void checkNotClosed() {
    if (isClosed.get()) {
      throw Exceptions.alreadyClosed(this);
    }
  }

  protected final boolean isClosed() {
    return isClosed.get();
  }

  protected final boolean getAndSetClosed() {
    return isClosed.getAndSet(true);
  }
}
