/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
/// Kotlin integration for [MiniPack](https://odenix.github.io/minipack/),
/// a modern Java library for reading and writing the MessagePack serialization format.
module io.github.odenix.minipack.kotlin {
  exports io.github.odenix.minipack.kotlin;

  requires io.github.odenix.minipack.core;
  requires kotlin.stdlib;
}
