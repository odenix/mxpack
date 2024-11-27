# Introduction

MiniPack is a modern, efficient, and extensible Java library
for reading and writing the [MessagePack](https://msgpack.org/) serialization format.

The Kotlin examples on this website use MiniPack's [Kotlin integration](kotlin-integration.md).

## Example

The following code writes a string and number to `path`, then reads them back:

/// tab | Java
```{.java}
-8<- "HelloMiniPack.java:snippet"
```

1. Creates an `:::java org.minipack.core.MessageWriter` that writes to `:::java out`.
   The `:::java try` block ensures that the writer is always closed and not used thereafter.
2. Encodes string `:::java "Hello, MiniPack!"` and writes it to `:::java out`.
3. Encodes number `:::java 42` and writes it to `:::java out`.
4. Creates an `:::java org.minipack.core.MessageReader` that reads from `:::java in`.
   The `:::java try` block ensures that the reader is always closed and not used thereafter.
5. Reads a string from `:::java in` and decodes it to `:::java "Hello, MiniPack!"`.
6. Reads a number from `:::java in` and decodes it to `:::java 42`.
///

/// tab | Kotlin
```{.kotlin}
-8<- "HelloMiniPack.kt:snippet"
```

1. Creates an `:::kotlin org.minipack.core.MessageWriter` that writes to `:::kotlin out`.
   The preferred Kotlin way to create a writer is `:::kotlin org.minipack.kotlin.MessageWriters.of()`.
   The `:::kotlin use {}` block ensures that the writer is always closed and not used thereafter.
2. Encodes string `:::kotlin "Hello, MiniPack!"` and writes it to `:::kotlin out`.
3. Encodes number `:::kotlin 42` and writes it to `:::kotlin out`.
4. Creates an `:::kotlin org.minipack.core.MessageReader` that reads from `:::kotlin in`.
   The preferred Kotlin way to create a reader is `:::kotlin org.minipack.kotlin.MessageReaders.of()`.
   The `:::kotlin use {}` block ensures that the reader is always closed and not used thereafter.
5. Reads a string from `:::kotlin in` and decodes it to `:::kotlin "Hello, MiniPack!"`.
6. Reads a number from `:::kotlin in` and decodes it to `:::kotlin 42`.
///

## License

Copyright 2024 the MiniPack contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

