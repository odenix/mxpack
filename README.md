[![Build Status](https://img.shields.io/github/actions/workflow/status/translatenix/minipack/run-dev-build)](https://github.com/translatenix/minipack/actions/workflows/run-dev-build.yml)
[![GitHub license](https://img.shields.io/github/license/translatenix/minipack)](https://github.com/translatenix/minipack/blob/main/LICENSE.txt)
# MiniPack

A modern, small, and efficient Java implementation of the [MessagePack](https://msgpack.org/) serialization format.

MiniPack requires Java 17+ and uses semantic versioning.

## MiniPack at a Glance

* Complete implementation of the MessagePack binary serialization format.
* Clean and modern API with [JSpecify](https://github.com/jspecify/jspecify) nullness annotations.
* Designed to be correct, efficient, and extensible.
* Embraces Java NIO: Heap and direct byte buffers, channels, channel transfers, gathering writes, buffer pooling.
* Fast, correct, and customizable string encoding/decoding with java.nio.charset.CharsetEncoder/CharsetDecoder.
* Optimized handling of strings used as identifiers.
* Small JAR size (~50 KB).
* No use of reflection or internal/unsafe JDK classes.
* Ships as Java (JPMS) module.
* No dependencies other than [JSpecify](https://central.sonatype.com/artifact/org.jspecify/jspecify) (~3 KB).
* Compatible with [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/).
* Thoroughly benchmarked with [JMH](https://github.com/openjdk/jmh).
* Thoroughly tested with [jqwik](https://jqwik.net/).
