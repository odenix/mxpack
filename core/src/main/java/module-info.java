/**
 * A modern, small, and efficient Java implementation of the <a
 * href="https://msgpack.org/">MessagePack</a> binary serialization format.
 */
module org.minipack.core {
  exports org.minipack.core;
  exports org.minipack.core.internal;

  requires static transitive org.jspecify;
}
