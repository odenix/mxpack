[![Build Status](https://img.shields.io/github/actions/workflow/status/translatenix/minipack/run-dev-build)](https://github.com/translatenix/minipack/actions/workflows/run-dev-build.yml)
[![GitHub license](https://img.shields.io/github/license/translatenix/minipack)](https://github.com/translatenix/minipack/blob/main/LICENSE.txt)
# MiniPack

A modern, small, and efficient implementation of the [MessagePack](https://msgpack.org/) serialization format for Java 17 and higher.

minipack uses semantic versioning.

## Reasons to choose MiniPack

* Clean API with [JSpecify](https://github.com/jspecify/jspecify) nullness annotations.
* No use of Java reflection or unsafe APIs.
* Small JAR size (less than 50 KB).
* Ships as Java (JPMS) module.
* No dependencies other than [JSpecify](https://central.sonatype.com/artifact/org.jspecify/jspecify), which can be excluded.
* Compatible with [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/).
* Designed with efficiency in mind.

## Reasons not to choose MiniPack

* minipack hasn't reached 1.0.0.
* minipack is a low-level MessagePack library and doesn't support 
  features such as mapping messages to Java objects.