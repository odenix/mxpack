# Features

## Current Features

* Complete implementation of the MessagePack [specification](https://github.com/msgpack/msgpack/blob/master/spec.md)
* Designed to be safe, efficient, and extensible
* Modern, interface based API with [JSpecify](https://github.com/jspecify/jspecify) nullness annotations
* Embraces Java NIO
    * Heap and direct byte buffers
    * Buffer pooling
    * Channel transfers
    * Gathering writes
* Fast and safe string encoding/decoding
    * Malformed and unmappable characters are replaced with `\uFFFD`      
* Small JAR size (~70 KB)
* No use of reflection or internal/unsafe JDK classes
* Ships as Java module (JPMS)
* No dependencies other than [JSpecify](https://central.sonatype.com/artifact/org.jspecify/jspecify) (~3 KB, optional)
* Compatible with [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/)
* Thoroughly tested with [jqwik](https://jqwik.net/)
    * Tested against MessagePack's [Java reference implementation](https://github.com/msgpack/msgpack-java)
* Thoroughly benchmarked with [JMH](https://github.com/openjdk/jmh)
    * Benchmarked against MessagePack's Java reference implementation

## Planned Features

* Optimized handling of strings used as identifiers
* Optimized handling of maps used as objects
* Integration with [jackson-databind](https://github.com/FasterXML/jackson-databind)
* Integration with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)