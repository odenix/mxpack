/**
 * A modern, small, and efficient Java implementation of the <a
 * href="https://msgpack.org/">MessagePack</a> binary serialization format.
 */
import org.jspecify.annotations.NullMarked;

@NullMarked
module org.minipack.core {
  exports org.minipack.core;

  requires static transitive org.jspecify;
}
