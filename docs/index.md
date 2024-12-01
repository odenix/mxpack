# Introduction

MxPack is a modern Java library for reading and writing the [MessagePack](https://msgpack.org/) serialization format.

The Kotlin examples on this website use MxPack's [Kotlin integration](kotlin-integration.md).

## Example

The following code writes a string and number to `path`, then reads them back:

/// tab | Java
```{.java}
-8<- "HelloMxPack.java:snippet"
```

1. Creates a `:::java MessageWriter` that writes to `:::java out`.
   The `:::java try` block ensures that the writer is always closed and not used thereafter.
2. Encodes string `:::java "Hello, MxPack!"` and writes it to `:::java out`.
3. Encodes number `:::java 42` and writes it to `:::java out`.
4. Creates a `:::java MessageReader` that reads from `:::java in`.
   The `:::java try` block ensures that the reader is always closed and not used thereafter.
5. Reads a string from `:::java in` and decodes it to `:::java "Hello, MxPack!"`.
6. Reads a number from `:::java in` and decodes it to `:::java 42`.
///

/// tab | Kotlin
```{.kotlin}
-8<- "HelloMxPack.kt:snippet"
```

1. Creates a `:::kotlin MessageWriter` that writes to `:::kotlin out`.
   The preferred Kotlin way to create a writer is `:::kotlin MessageWriters.of()` (note the plural form).
   The `:::kotlin use {}` block ensures that the writer is always closed and not used thereafter.
2. Encodes string `:::kotlin "Hello, MxPack!"` and writes it to `:::kotlin out`.
3. Encodes number `:::kotlin 42` and writes it to `:::kotlin out`.
4. Creates a `:::kotlin MessageReader` that reads from `:::kotlin in`.
   The preferred Kotlin way to create a reader is `:::kotlin MessageReaders.of()` (note the plural form).
   The `:::kotlin use {}` block ensures that the reader is always closed and not used thereafter.
5. Reads a string from `:::kotlin in` and decodes it to `:::kotlin "Hello, MxPack!"`.
6. Reads a number from `:::kotlin in` and decodes it to `:::kotlin 42`.
///

## License

Copyright 2024 the MxPack project authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

