/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
/// Kotlin integration for [MxPack](https://github.com/odenix/mxpack),
/// a modern Java library for reading and writing the [MessagePack](https://msgpack.org/) serialization format.
///
/// For further documentation, see the [MxPack website](https://odenix.org/mxpack/kotlin-integration/).
module org.odenix.mxpack.kotlin {
  exports org.odenix.mxpack.kotlin;

  requires org.odenix.mxpack.core;
  requires kotlin.stdlib;
}
