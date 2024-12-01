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

The Kotlin API's factory methods mirror the Java API's factory methods
but use named arguments with default values instead of option builders.

For example, `:::kotlin BufferAllocators.ofPooled()` (note the plural form)
mirrors `:::kotlin BufferAllocator.ofPooled()`.

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
