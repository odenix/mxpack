# Examples

## Reading Data

### Read from file

Also see: [Write to file](#write-to-file)

/// tab | Java
```{.java}
-8<- "ReadFromFile.java:snippet"
```

1. `:::java java.nio.channels.FileChannel` is the most efficient way to read a file.
   ///

/// tab | Kotlin

```{.kotlin}
-8<- "ReadFromFile.kt:snippet"
```

1. `:::java java.nio.channels.FileChannel` is the most efficient way to read a file.
   ///

### Read from channel

Also see: [Write to channel](#write-to-channel)

/// tab | Java
```{.java}
-8<- "ReadFromChannel.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "ReadFromChannel.kt:snippet"
```
///

### Read from stream

Also see: [Write to stream](#write-to-stream)

/// tab | Java
```{.java}
-8<- "ReadFromStream.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "ReadFromStream.kt:snippet"
```
///

### Read from buffer

Also see: [Write to buffer](#write-to-buffer)

/// tab | Java
```{.java}
-8<- "ReadFromBuffer.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "ReadFromBuffer.kt:snippet"
```
///

### Read array

Also see: [Write array](#write-array)

/// tab | Java
```{.java}
-8<- "ReadArray.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "ReadArray.kt:snippet"
```
///

### Read map

Also see: [Write map](#write-map)

/// tab | Java
```{.java}
-8<- "ReadMap.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "ReadMap.kt:snippet"
```
///

### Set reader options

Also see: [Set writer options](#set-writer-options)

/// tab | Java
```{.java}
-8<- "SetReaderOptions.java:snippet"
```

1. Sets reader options by calling `:::java OptionBuilder` methods.
   All options are optional; this example shows their defaults.
///

/// tab | Kotlin

```{.kotlin}
-8<- "SetReaderOptions.kt:snippet"
```

1. Sets reader options by setting named method parameters.
   All options are optional; this example shows their defaults.
///

## Writing Data

### Write to file

Also see: [Read from file](#read-from-file)

/// tab | Java
```{.java}
-8<- "WriteToFile.java:snippet"
```

1. `:::java java.nio.channels.FileChannel` is the most efficient way to write a file.
///

/// tab | Kotlin

```{.kotlin}
-8<- "WriteToFile.kt:snippet"
```

1. `:::kotlin java.nio.channels.FileChannel` is the most efficient way to write a file.
///

### Write to channel

Also see: [Read from channel](#read-from-channel)

/// tab | Java
```{.java}
-8<- "WriteToChannel.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "WriteToChannel.kt:snippet"
```
///

### Write to stream

Also see: [Read from stream](#read-from-stream)

/// tab | Java
```{.java}
-8<- "WriteToStream.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "WriteToStream.kt:snippet"
```
///

### Write to buffer

Also see: [Read from buffer](#read-from-buffer)

/// tab | Java
```{.java}
-8<- "WriteToBuffer.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "WriteToBuffer.kt:snippet"
```
///

### Write array

Also see: [Read array](#read-array)

/// tab | Java
```{.java}
-8<- "WriteArray.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "WriteArray.kt:snippet"
```
///

### Write map

Also see: [Read map](#read-map)

/// tab | Java
```{.java}
-8<- "WriteMap.java:snippet"
```
///

/// tab | Kotlin

```{.kotlin}
-8<- "WriteMap.kt:snippet"
```
///

### Set writer options

Also see: [Set reader options](#set-reader-options)

/// tab | Java
```{.java}
-8<- "SetWriterOptions.java:snippet"
```

1. Sets writer options by calling `:::java OptionBuilder` methods.
   All options are optional; this example shows their defaults.
///

/// tab | Kotlin

```{.kotlin}
-8<- "SetWriterOptions.kt:snippet"
```

1. Sets writer options by settings named method parameters.
   All options are optional; this example shows their defaults.
///

## Buffer Allocation

### Use default allocator

/// tab | Java
```{.java}
-8<- "UseDefaultAllocator.java:snippet"
```

1. If no allocator is set when a reader (or writer) is created,
   an unpooled allocator with default options is used.
///

/// tab | Kotlin

```{.kotlin}
-8<- "UseDefaultAllocator.kt:snippet"
```

1. If no allocator is set when a reader (or writer) is created,
   an unpooled allocator with default options is used.
///

### Use unpooled allocator

/// tab | Java
```{.java}
-8<- "UseUnpooledAllocator.java:snippet"
```

1. Creates an allocator that returns a new buffer every time
   `:::java getByteBuffer()` or `:::java getCharBuffer()` is called.
2. Sets allocator options by calling `:::java UnpooledOptionBuilder` methods.
   All options are optional; this example shows their defaults.
3. Sets the same allocator every time a message reader (or writer) is created.
   Allocators can be safely shared between multiple threads.
4. When the allocator is no longer used, it should be closed.
///

/// tab | Kotlin

```{.kotlin}
-8<- "UseUnpooledAllocator.kt:snippet"
```

1. Creates an allocator that returns a new buffer every time
   `:::java getByteBuffer()` or `:::java getCharBuffer()` is called.
2. Sets allocator options by setting named method parameters.
   All options are optional; this example shows their defaults.
3. Sets the same allocator every time a message reader (or writer) is created.
   Allocators can be safely shared between multiple threads.
4. When the allocator is no longer used, it should be closed.
///

### Use pooled allocator

/// tab | Java
```{.java}
-8<- "UsePooledAllocator.java:snippet"
```

1. Creates an allocator that reduces buffer allocations by maintaining a buffer pool.
2. Sets allocator options by calling `:::java PooledOptionBuilder` methods.
   All options are optional; this example shows their defaults.
3. Sets the same allocator every time a message reader (or writer) is created.
   Allocators can be safely shared between multiple threads.
4. When the allocator is no longer used, it should be closed to free its buffer pool.
///

/// tab | Kotlin

```{.kotlin}
-8<- "UsePooledAllocator.kt:snippet"
```

1. Creates an allocator that reduces buffer allocations by maintaining a buffer pool.
2. Sets allocator options by setting named method parameters.
   All options are optional; this example shows their defaults.
3. Sets the same allocator every time a message reader (or writer) is created.
   Allocators can be safely shared between multiple threads.
4. When the allocator is no longer used, it should be closed to free its buffer pool.
///

## Kotlin Integration

### Read unsigned integers

See [Kotlin Integration](kotlin-integration.md/#read-unsigned-integers).

### Write unsigned integers

See [Kotlin Integration](kotlin-integration.md/#write-unsigned-integers).

[1]: https://translatenix.github.io/minipack/api/org/minipack/java/MessageWriter.html
[2]: https://translatenix.github.io/minipack/api/org/minipack/java/MessageReader.html
[3]: https://translatenix.github.io/minipack/api/org/minipack/java/BufferAllocator#of.html
[4]: https://translatenix.github.io/minipack/api/org/minipack/kotlin/BufferAllocator#of.html
