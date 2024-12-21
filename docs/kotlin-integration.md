# Kotlin Integration

## Requirements

* JDK 17 or later
* Kotlin 2.0 or later

## Installation

/// tab | pom.xml
```xml
<dependency>
  <groupId>org.odenix.mxpack</groupId>
  <artifactId>mxpack-kotlin</artifactId>
  <version>${mxpackVersion}</version>
</dependency>
```
///

/// tab | build.gradle.kts
```{.kotlin}
dependencies {
  implementation("org.odenix.mxpack:mxpack-kotlin:${mxpackVersion}")
}
```
///

/// tab | build.gradle
```groovy
dependencies {
  implementation "org.odenix.mxpack:mxpack-kotlin:${mxpackVersion}"
}
```
///

## Features

### Factory methods

The Kotlin API offers factory methods that replace the Java API's factory methods.
The factory methods are named after the Java interfaces they return.
Instead of option handlers, they use named arguments with default values.

For example, to create a `:::kotlin MessageReader`, use the Kotlin factory method
`:::kotlin MessageReader()` instead of the Java factory method `:::kotlin MessageReader.of()`.
Occasionally, the Kotlin factory method name differs from the Java interface name.
For example, to create a pooled `:::kotlin BufferAllocator`, use the Kotlin factory method
`:::kotlin PooledBufferAllocator()` instead of the Java factory method `:::kotlin BufferAllocator.ofPooled()`.

/// tab | Java
```{.java}
-8<- "KotlinFactoryMethods.java:snippet"
```
///

/// tab | Kotlin
```{.java}
-8<- "KotlinFactoryMethods.kt:snippet"
```
///

### Extension methods

#### Read unsigned integers

```{.kotlin}
-8<- "KotlinReadUnsigned.kt:snippet"
```

#### Write unsigned integers

```{.kotlin}
-8<- "KotlinWriteUnsigned.kt:snippet"
```
