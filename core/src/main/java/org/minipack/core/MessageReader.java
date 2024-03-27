/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.time.Instant;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;
import org.minipack.core.internal.Exceptions;
import org.minipack.core.internal.RequestedType;
import org.minipack.core.internal.ValueFormat;

/**
 * Reads values encoded in the <a href="https://msgpack.org/">MessagePack</a> binary serialization
 * format.
 *
 * <p>To create a new {@code MessageReader}, use a {@linkplain #builder() builder}. To read a value,
 * call one of the {@code readXYZ()} methods. To determine the next value's type, call {@link
 * #nextType()}. To close this reader, call {@link #close()}.
 *
 * <p>Unless otherwise noted, methods throw {@link IOException} if an I/O error occurs, and {@link
 * ReaderException} if some other error occurs.
 */
public final class MessageReader implements Closeable {
  private static final int MIN_BUFFER_SIZE = 9;
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
  private static final int MAX_STRING_SIZE = 1024 * 1024;
  private static final int MAX_IDENTIFIER_STRING_SIZE = 1024;
  private static final int MAX_IDENTIFIER_CACHE_SIZE = 1024 * 8;
  private static final byte TIMESTAMP_EXTENSION_TYPE = -1;
  private static final long LOWER_34_BITS_MASK = 0x3ffffffffL;

  private final MessageSource source;
  private final ByteBuffer buffer;
  private final Decoder<String> stringDecoder;
  private final Decoder<String> identifierDecoder;

  /** A builder of {@link MessageReader}. */
  public static final class Builder {
    private @Nullable MessageSource source;
    private @Nullable ByteBuffer buffer;
    private @Nullable Decoder<String> stringDecoder;
    private Decoder<String> identifierDecoder =
        Decoder.identifierDecoder(MAX_IDENTIFIER_STRING_SIZE, MAX_IDENTIFIER_CACHE_SIZE);

    /** Sets the underlying source to read from. */
    public Builder source(MessageSource source) {
      this.source = source;
      return this;
    }

    /** Equivalent to {@code source(MessageSource.of(stream))}. */
    public Builder source(InputStream stream) {
      return source(MessageSource.of(stream));
    }

    /** Equivalent to {@code source(MessageSource.of(channel))}. */
    public Builder source(ReadableByteChannel channel) {
      return source(MessageSource.of(channel));
    }

    /**
     * Sets the buffer to use for reading from the underlying message {@linkplain MessageSource
     * source}.
     *
     * <p>The buffer's {@linkplain ByteBuffer#capacity() capacity} determines the maximum number of
     * bytes that will be read at once from the source.
     *
     * <p>If not set, defaults to {@code ByteBuffer.allocate(1024 * 8)}.
     */
    public Builder buffer(ByteBuffer buffer) {
      this.buffer = buffer;
      return this;
    }

    public Builder stringDecoder(Decoder<String> decoder) {
      stringDecoder = decoder;
      return this;
    }

    public Builder identifierDecoder(Decoder<String> decoder) {
      identifierDecoder = decoder;
      return this;
    }

    /** Builds a {@code MessageReader} from this builder's current state. */
    public MessageReader build() {
      return new MessageReader(this);
    }
  }

  /** Creates a new {@code MessageReader} builder. */
  public static Builder builder() {
    return new Builder();
  }

  private MessageReader(Builder builder) {
    if (builder.source == null) {
      throw Exceptions.sourceRequired();
    }
    source = builder.source;
    buffer =
        builder.buffer != null
            ? builder.buffer.position(0).limit(0)
            : ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).limit(0);
    if (buffer.capacity() < MIN_BUFFER_SIZE) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_SIZE);
    }
    stringDecoder =
        builder.stringDecoder != null
            ? builder.stringDecoder
            : Decoder.stringDecoder(buffer.capacity() * 2, MAX_STRING_SIZE);
    identifierDecoder = builder.identifierDecoder;
  }

  /** Returns the type of the next value to be read. */
  public ValueType nextType() throws IOException {
    return ValueFormat.toType(nextFormat());
  }

  /** Reads a nil (null) value. */
  public void readNil() throws IOException {
    var format = getByte();
    if (format != ValueFormat.NIL) {
      throw Exceptions.typeMismatch(format, RequestedType.NIL);
    }
  }

  /** Reads a boolean value. */
  public boolean readBoolean() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.TRUE -> true;
      case ValueFormat.FALSE -> false;
      default -> throw Exceptions.typeMismatch(format, RequestedType.BOOLEAN);
    };
  }

  /** Reads an integer value that fits into a Java byte. */
  public byte readByte() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.INT8 -> getByte();
      case ValueFormat.INT16 -> {
        var value = getShort();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case ValueFormat.INT32 -> {
        var value = getInt();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case ValueFormat.INT64 -> {
        var value = getLong();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case ValueFormat.UINT8 -> {
        var value = getByte();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case ValueFormat.UINT16 -> {
        var value = getShort();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case ValueFormat.UINT32 -> {
        var value = getInt();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case ValueFormat.UINT64 -> {
        var value = getLong();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      default -> {
        if (ValueFormat.isFixInt(format)) yield format;
        throw Exceptions.typeMismatch(format, RequestedType.BYTE);
      }
    };
  }

  /** Reads an integer value that fits into a Java short. */
  public short readShort() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.INT8 -> getByte();
      case ValueFormat.INT16 -> getShort();
      case ValueFormat.INT32 -> {
        var value = getInt();
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      case ValueFormat.INT64 -> {
        var value = getLong();
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      case ValueFormat.UINT8 -> getUByte();
      case ValueFormat.UINT16 -> {
        var value = getShort();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      case ValueFormat.UINT32 -> {
        var value = getInt();
        if (value >= 0 && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      case ValueFormat.UINT64 -> {
        var value = getLong();
        if (value >= 0 && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      default -> {
        if (ValueFormat.isFixInt(format)) yield format;
        throw Exceptions.typeMismatch(format, RequestedType.SHORT);
      }
    };
  }

  /** Reads an integer value that fits into a Java int. */
  public int readInt() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.INT8 -> getByte();
      case ValueFormat.INT16 -> getShort();
      case ValueFormat.INT32 -> getInt();
      case ValueFormat.INT64 -> {
        var value = getLong();
        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) yield (int) value;
        throw Exceptions.integerOverflow(value, RequestedType.INT);
      }
      case ValueFormat.UINT8 -> getUByte();
      case ValueFormat.UINT16 -> getUShort();
      case ValueFormat.UINT32 -> {
        var value = getInt();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, RequestedType.INT);
      }
      case ValueFormat.UINT64 -> {
        var value = getLong();
        if (value >= 0 && value <= Integer.MAX_VALUE) yield (int) value;
        throw Exceptions.integerOverflow(value, RequestedType.INT);
      }
      default -> {
        if (ValueFormat.isFixInt(format)) yield format;
        throw Exceptions.typeMismatch(format, RequestedType.INT);
      }
    };
  }

  /** Reads an integer value that fits into a Java long. */
  public long readLong() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.INT8 -> getByte();
      case ValueFormat.INT16 -> getShort();
      case ValueFormat.INT32 -> getInt();
      case ValueFormat.INT64 -> getLong();
      case ValueFormat.UINT8 -> getUByte();
      case ValueFormat.UINT16 -> getUShort();
      case ValueFormat.UINT32 -> getUInt();
      case ValueFormat.UINT64 -> {
        var value = getLong();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, RequestedType.LONG);
      }
      default -> {
        if (ValueFormat.isFixInt(format)) yield format;
        throw Exceptions.typeMismatch(format, RequestedType.LONG);
      }
    };
  }

  /** Reads a floating point value that fits into a Java float. */
  public float readFloat() throws IOException {
    source.ensureRemaining(buffer, 5);
    var format = buffer.get();
    if (format == ValueFormat.FLOAT32) return buffer.getFloat();
    throw Exceptions.typeMismatch(format, RequestedType.FLOAT);
  }

  /** Reads a floating point value that fits into a Java double. */
  public double readDouble() throws IOException {
    source.ensureRemaining(buffer, 9);
    var format = buffer.get();
    if (format == ValueFormat.FLOAT64) return buffer.getDouble();
    throw Exceptions.typeMismatch(format, RequestedType.DOUBLE);
  }

  /** Reads a timestamp value. */
  public Instant readTimestamp() throws IOException {
    var header = readExtensionHeader();
    if (header.type() != TIMESTAMP_EXTENSION_TYPE) {
      throw Exceptions.extensionTypeMismatch(TIMESTAMP_EXTENSION_TYPE, header.type());
    }
    return switch (header.length()) {
      case 4 -> Instant.ofEpochSecond(source.getInt(buffer));
      case 8 -> {
        var value = source.getLong(buffer);
        var nanos = value >>> 34;
        var seconds = value & LOWER_34_BITS_MASK;
        yield Instant.ofEpochSecond(seconds, nanos);
      }
      case 12 -> {
        var nanos = source.getInt(buffer);
        var seconds = source.getLong(buffer);
        yield Instant.ofEpochSecond(seconds, nanos);
      }
      default -> throw Exceptions.invalidTimestampLength(header.length());
    };
  }

  /**
   * Reads a string value.
   *
   * <p>To read a string as a sequence of bytes, use {@link #readStringHeader()} together with
   * {@link #readPayload(BiConsumer)}.
   */
  public String readString() throws IOException {
    return stringDecoder.decode(buffer, source, this);
  }

  public String readIdentifier() throws IOException {
    return identifierDecoder.decode(buffer, source, this);
  }

  public <T> T readValue(Decoder<T> decoder) throws IOException {
    return decoder.decode(buffer, source, this);
  }

  /**
   * Starts reading an array value.
   *
   * <p>A call to this method <i>must</i> be followed by {@code n} calls that read the array's
   * elements, where {@code n} is the number of array elements returned by this method.
   */
  public int readArrayHeader() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.ARRAY16 -> getLength16();
      case ValueFormat.ARRAY32 -> getLength32(ValueType.ARRAY);
      default -> {
        if (ValueFormat.isFixArray(format)) {
          yield ValueFormat.getFixArrayLength(format);
        }
        throw Exceptions.typeMismatch(format, RequestedType.ARRAY);
      }
    };
  }

  /**
   * Starts reading a map value.
   *
   * <p>A call to this method <i>must</i> be followed by {@code n*2} calls that alternately read the
   * map's keys and values, where {@code n} is the number of map entries returned by this method.
   */
  public int readMapHeader() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.MAP16 -> getLength16();
      case ValueFormat.MAP32 -> getLength32(ValueType.MAP);
      default -> {
        if (ValueFormat.isFixMap(format)) {
          yield ValueFormat.getFixMapLength(format);
        }
        throw Exceptions.typeMismatch(format, RequestedType.MAP);
      }
    };
  }

  /**
   * Starts reading a binary value.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   */
  public int readBinaryHeader() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.BIN8 -> getLength8();
      case ValueFormat.BIN16 -> getLength16();
      case ValueFormat.BIN32 -> getLength32(ValueType.BINARY);
      default -> throw Exceptions.typeMismatch(format, RequestedType.BINARY);
    };
  }

  /**
   * Starts reading a string value as a sequence of bytes.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   *
   * <p>This method is a low-level alternative to {@link #readString()}.
   */
  public int readStringHeader() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.STR8 -> getLength8();
      case ValueFormat.STR16 -> getLength16();
      case ValueFormat.STR32 -> getLength32(ValueType.STRING);
      default -> {
        if (ValueFormat.isFixStr(format)) {
          yield ValueFormat.getFixStrLength(format);
        }
        throw Exceptions.typeMismatch(format, RequestedType.STRING);
      }
    };
  }

  /**
   * Starts reading an extension value.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   */
  public ExtensionHeader readExtensionHeader() throws IOException {
    var format = getByte();
    return switch (format) {
      case ValueFormat.FIXEXT1 -> new ExtensionHeader(1, getByte());
      case ValueFormat.FIXEXT2 -> new ExtensionHeader(2, getByte());
      case ValueFormat.FIXEXT4 -> new ExtensionHeader(4, getByte());
      case ValueFormat.FIXEXT8 -> new ExtensionHeader(8, getByte());
      case ValueFormat.FIXEXT16 -> new ExtensionHeader(16, getByte());
      case ValueFormat.EXT8 -> new ExtensionHeader(getLength8(), getByte());
      case ValueFormat.EXT16 -> new ExtensionHeader(getLength16(), getByte());
      case ValueFormat.EXT32 -> new ExtensionHeader(getLength32(ValueType.EXTENSION), getByte());
      default -> throw Exceptions.typeMismatch(format, RequestedType.EXTENSION);
    };
  }

  public void readPayload(BiConsumer<ByteBuffer, MessageSource> reader) {
    reader.accept(buffer, source);
  }

  /** Closes the underlying message {@linkplain MessageSource source}. */
  @Override
  public void close() throws IOException {
    source.close();
  }

  private byte getByte() throws IOException {
    return source.getByte(buffer);
  }

  private short getShort() throws IOException {
    return source.getShort(buffer);
  }

  private int getInt() throws IOException {
    return source.getInt(buffer);
  }

  private long getLong() throws IOException {
    return source.getLong(buffer);
  }

  // non-private for testing
  byte nextFormat() throws IOException {
    return source.nextByte(buffer);
  }

  private short getUByte() throws IOException {
    source.ensureRemaining(buffer, 1);
    return (short) (buffer.get() & 0xff);
  }

  private int getUShort() throws IOException {
    source.ensureRemaining(buffer, 2);
    return buffer.getShort() & 0xffff;
  }

  private long getUInt() throws IOException {
    source.ensureRemaining(buffer, 4);
    return buffer.getInt() & 0xffffffffL;
  }

  private short getLength8() throws IOException {
    return getUByte();
  }

  private int getLength16() throws IOException {
    return getUShort();
  }

  private int getLength32(ValueType type) throws IOException {
    var length = getInt();
    if (length < 0) {
      throw Exceptions.lengthOverflow(length & 0xffffffffL, type);
    }
    return length;
  }
}
