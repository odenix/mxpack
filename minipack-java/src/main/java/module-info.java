/**
 * A modern Java implementation of the <a href="https://msgpack.org/">MessagePack</a> serialization
 * format.
 */
import org.jspecify.annotations.NullMarked;

@NullMarked
module org.minipack.java {
  exports org.minipack.java;

  requires static transitive org.jspecify;
}
