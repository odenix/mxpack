/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal.util;

import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;

/// Concurrent lock-free pool data structure.
///
/// Supports two operations -- adding an element, and removing an element. The
/// [#add(Object)] operation adds an element into the pool. The [#get()]
/// operation returns one of the elements previously added to the pool. There is no guarantee about
/// the order in which the elements are returned by [#get()]. The guarantee is that
/// an element will only be returned by [#get()] as many times as it was previously
/// added to the pool by calling [#add(Object)]. If there are no more elements to
/// return, it will return `null`. Both operations are lock-free and linearizable -- this data
/// structure is intended for use by multiple threads.
///
/// The internal implementation is a simple Treiber stack.
///
/// @param <T> Type of the elements in this pool.
public class LockFreePool<T> {
  /** The top-of-the-Treiber-stack pointer. */
  private final AtomicReference<@Nullable Node<T>> head;

  public LockFreePool() {
    this.head = new AtomicReference<>();
  }

  /// Returns a previously added element.
  ///
  /// This method returns a previously added element only once to some caller. If the element was
  /// added multiple times, calling this method will return that element as many times before
  /// returning `null`.
  ///
  /// This method does not do any object allocations.
  ///
  /// @return A previously added element, or `null` if there are no previously added elements
  ///     that have not been already returned.
  public @Nullable T get() {
    while (true) {
      Node<T> oldHead = head.get();
      if (oldHead == null) {
        return null;
      }
      Node<T> newHead = oldHead.tail;
      if (head.compareAndSet(oldHead, newHead)) {
        return oldHead.element;
      }
    }
  }

  /// Adds an element to this pool.
  ///
  /// An element can be added multiple times to the pool, but in this case may be returned as many
  /// times by [#get()].
  ///
  /// This method internally allocates objects on the heap.
  ///
  /// @param element An element to add to the pool.
  public void add(T element) {
    while (true) {
      Node<T> oldHead = head.get();
      Node<T> newHead = new Node<>(element, oldHead);
      if (head.compareAndSet(oldHead, newHead)) {
        return;
      }
    }
  }

  /// Internal wrapper node used to wrap the element and the `tail` pointer.
  ///
  /// @param element Element stored in this node.
  /// @param tail    Pointer to the tail of the linked list, or `null` if the end of the list.
  private record Node<E>(E element, LockFreePool.@Nullable Node<E> tail) {
    private Node(E element, @Nullable Node<E> tail) {
      this.element = element;
      this.tail = tail;
    }
  }
}
