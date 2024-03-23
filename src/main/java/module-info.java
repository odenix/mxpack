/**
 * A modern, small, and efficient implementation of the <a
 * href="https://msgpack.org/">MessagePack</a> binary serialization format for Java 17 and higher.
 */
module org.translatenix.msgpack {
  exports org.translatenix.minipack;
  exports org.translatenix.minipack.internal;

  requires org.jspecify;
}
