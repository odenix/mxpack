![GitHub license](https://img.shields.io/github/license/translatenix/minipack)

# minipack

A modern, small, and efficient implementation of the [MessagePack](https://msgpack.org/) serialization format for Java 17 and higher.

Maven repository: TODO

API documentation: TODO

minipack uses semantic versioning.

## Reasons to choose minipack

* Clean API with [JSpecify](https://github.com/jspecify/jspecify) nullness annotations.
* No use of Java reflection or unsafe APIs.
* Small JAR size (less than 50 KB).
* Ships as Java (JPMS) module.
* No dependencies other than [JSpecify](https://central.sonatype.com/artifact/org.jspecify/jspecify), which can be excluded if necessary.
* Compatible with [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/).
* Designed with efficiency in mind.

## Reasons not to choose minipack

* minipack hasn't reached 1.0.0.
* minipack is a low-level MessagePack library and doesn't support 
  features such as mapping messages to Java objects.