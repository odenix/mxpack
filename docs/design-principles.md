# Design Principles

* The Java API largely consists of interfaces with factory methods named `of` or `ofXxx`.
* Nullable types are annotated with [org.jspecify.Nullable][1].
  Types not annotated with `Nullable` are implicitly [org.jspecify.NonNull][2].
  Non-nullness of method arguments is *not* enforced through runtime checks.
* MxPack throws the exception types listed in [MxPackException][3].
* MxPack embraces Java NIO. 
  For best performance, use [MessageSource][4]s and [MessageSink][5]s
  backed by [java.nio.channels.Channel][6]s.
  Byte buffers are represented as [java.nio.ByteBuffer][7] and [LeasedByteBuffer][8].
  If frequent buffer allocations are a concern,
  share a single *pooled* [BufferAllocator][9] between message sources and sinks.

[1]: https://jspecify.dev/docs/api/org/jspecify/annotations/Nullable.html
[2]: https://jspecify.dev/docs/api/org/jspecify/annotations/NonNull.html
[3]: https://odenix.org/mxpack/javadoc/org/odenix/mxpack/MxPackException.html
[4]: https://odenix.org/mxpack/javadoc/org/odenix/mxpack/MessageSource.html
[5]: https://odenix.org/mxpack/javadoc/org/odenix/mxpack/MessageSink.html
[6]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/channels/Channel.html
[7]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/ByteBuffer.html
[8]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/LeasedByteBuffer.html
[9]: https://odenix.org/mxpack/javdoc/org/odenix/mxpack/BufferAllocator.html