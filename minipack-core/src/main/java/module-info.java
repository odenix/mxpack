/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
import org.jspecify.annotations.NullMarked;

/// A modern, efficient, and extensible Java library for reading and writing
/// the <a href="https://msgpack.org/">MessagePack</a> serialization format.
///
/// For further documentation, see the [MiniPack website](https://translatenix.github.io/minipack/).
@NullMarked
module org.minipack.core {
  exports org.minipack.core;

  requires static transitive org.jspecify;
  requires java.desktop;
}
