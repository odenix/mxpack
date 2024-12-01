/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
import org.jspecify.annotations.NullMarked;

/// A modern Java library for reading and writing
/// the <a href="https://msgpack.org/">MessagePack</a> serialization format.
///
/// For further documentation, see the [MxPack website](https://odenix.org/mxpack/).
@NullMarked
module org.odenix.mxpack.core {
  exports org.odenix.mxpack.core;

  requires static transitive org.jspecify;
}
