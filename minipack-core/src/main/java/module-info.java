/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
import org.jspecify.annotations.NullMarked;

/// A modern Java library for reading and writing
/// the <a href="https://msgpack.org/">MessagePack</a> serialization format.
///
/// For further documentation, see the [MiniPack website](https://odenix.github.io/minipack/).
@NullMarked
module io.github.odenix.minipack.core {
  exports io.github.odenix.minipack.core;

  requires static transitive org.jspecify;
}
