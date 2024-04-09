import org.jspecify.annotations.NullMarked;

/**
 * Extensions for the minipack-core library.
 */
@NullMarked
module org.minipack.extension {
  exports org.minipack.extension;

  requires org.minipack.core;
  requires static transitive org.jspecify;
}
