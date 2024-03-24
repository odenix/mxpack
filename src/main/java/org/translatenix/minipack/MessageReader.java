/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.jspecify.annotations.Nullable;
import org.translatenix.minipack.internal.Exceptions;
import org.translatenix.minipack.internal.RequestedType;
import org.translatenix.minipack.internal.Utf8StringReader;
import org.translatenix.minipack.internal.ValueFormat;

/**
 * Reads messages encoded in the <a href="https://msgpack.org/">MessagePack</a> binary serialization
 * format.
 *
 * <p>To create a new {@code MessageReader}, use a {@linkplain #builder() builder}. To read a value,
 * call one of the {@code readXYZ()} methods. To determine the next value's type, call {@link
 * #nextType()}.
 *
 * <p>Unless otherwise noted, methods throw {@link ReaderException} if an error occurs. The most
 * common type of error is an I/O error.
 */
public final class MessageReader<T> implements Closeable {
  private static final int MIN_BUFFER_CAPACITY = 9;
  private static final int DEFAULT_BUFFER_CAPACITY = 1 << 13;
  private static final int DEFAULT_STRING_SIZE_LIMIT = 1 << 20;

  private final MessageSource source;
  private final ByteBuffer buffer;
  private final StringReader<T> stringReader;

  /** A builder of {@link MessageReader}. */
  public static final class Builder {
    private @Nullable MessageSource source;
    private @Nullable ByteBuffer buffer;

    /** Sets the underlying source to read from. */
    public Builder source(MessageSource source) {
      this.source = source;
      return this;
    }

    /** Shorthand for {@code source(MessageSource.of(stream))}. */
    public Builder source(InputStream stream) {
      return source(MessageSource.of(stream));
    }

    /** Shorthand for {@code source(MessageSource.of(channel))}. */
    public Builder source(ReadableByteChannel channel) {
      return source(MessageSource.of(channel));
    }

    /**
     * Sets the buffer to use for reading from the underlying message {@linkplain MessageSource
     * source}. The buffer's {@linkplain ByteBuffer#capacity() capacity} determines the maximum
     * number of bytes that will be read at once from the source.
     *
     * <p>If not set, defaults to {@code ByteBuffer.allocate(8192)}.
     */
    public Builder buffer(ByteBuffer buffer) {
      this.buffer = buffer;
      return this;
    }

    /** Creates a new {@code MessageReader} from this builder's current state. */
    public MessageReader<String> build() {
      return new MessageReader<>(this, new Utf8StringReader(DEFAULT_STRING_SIZE_LIMIT));
    }

    public <T> MessageReader<T> build(StringReader<T> stringReader) {
      return new MessageReader<>(this, stringReader);
    }
  }

  /** Creates a new {@code MessageWriter} builder. */
  public static Builder builder() {
    return new Builder();
  }

  private MessageReader(Builder builder, StringReader<T> stringReader) {
    if (builder.source == null) {
      throw Exceptions.sourceRequired();
    }
    this.source = builder.source;
    this.buffer =
        builder.buffer != null
            ? builder.buffer.position(0).limit(0)
            : ByteBuffer.allocate(DEFAULT_BUFFER_CAPACITY).limit(0);
    if (buffer.capacity() < MIN_BUFFER_CAPACITY) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_CAPACITY);
    }
    this.stringReader = stringReader;
  }

  /** Returns the type of the next value to be read. */
  public ValueType nextType() {
    ensureRemaining(1);
    // don't change position
    return ValueFormat.toType(buffer.get(buffer.position()));
  }

  /** Reads a nil (null) value. */
  public void readNil() {
    var format = getByte();
    if (format != ValueFormat.NIL) {
      throw Exceptions.typeMismatch(format, RequestedType.NIL);
    }
  }

  /** Reads a boolean value. */
  public boolean readBoolean() {
    var format = getByte();
    return switch (format) {
      case ValueFormat.TRUE -> true;
      case ValueFormat.FALSE -> false;
      default -> throw Exceptions.typeMismatch(format, RequestedType.BOOLEAN);
    };
  }

  /** Reads an integer value that fits into a Java byte. */
  public byte readByte() {
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
  public short readShort() {
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
  public int readInt() {
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
  public long readLong() {
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
        throw Exceptions.typeMismatch(format, RequestedType.INT);
      }
    };
  }

  /** Reads a floating point value that fits into a Java float. */
  public float readFloat() {
    var format = getByte();
    if (format == ValueFormat.FLOAT32) return getFloat();
    throw Exceptions.typeMismatch(format, RequestedType.FLOAT);
  }

  /** Reads a floating point value that fits into a Java double. */
  public double readDouble() {
    var format = getByte();
    if (format == ValueFormat.FLOAT64) return getDouble();
    throw Exceptions.typeMismatch(format, RequestedType.DOUBLE);
  }

  /**
   * Reads a string value.
   *
   * <p>For a lower-level way to read strings, see {@link #readRawStringHeader()}.
   */
  public T readString() {
    try {
      return stringReader.read(buffer, source);
    } catch (IOException e) {
      throw Exceptions.ioErrorReadingFromSource(e);
    }
  }

  /**
   * Starts reading an array value.
   *
   * <p>A call to this method MUST be followed by {@code n} calls that read the array's elements,
   * where {@code n} is the number of array elements returned by this method.
   */
  public int readArrayHeader() {
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
   * <p>A call to this method MUST be followed by {@code n*2} calls that alternately read the map's
   * keys and values, where {@code n} is the number of map entries returned by this method.
   */
  public int readMapHeader() {
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
   * <p>A call to this method MUST be followed by one or more calls to {@link #readPayload} that
   * read <i>exactly</i> the number of bytes returned by this method.
   */
  public int readBinaryHeader() {
    var format = getByte();
    return switch (format) {
      case ValueFormat.BIN8 -> getLength8();
      case ValueFormat.BIN16 -> getLength16();
      case ValueFormat.BIN32 -> getLength32(ValueType.BINARY);
      default -> throw Exceptions.typeMismatch(format, RequestedType.BINARY);
    };
  }

  /**
   * Starts reading a string value.
   *
   * <p>A call to this method MUST be followed by one or more calls to {@link #readPayload} that
   * read <i>exactly</i> the number of bytes returned by this method.
   *
   * <p>This method is a low-level alternative to {@link #readString()}. It can be useful in the
   * following cases:
   *
   * <ul>
   *   <li>There is no need to convert a MessagePack string's UTF-8 payload to {@code
   *       java.lang.String}.
   *   <li>Full control over conversion from UTF-8 to {@code java.lang.String} is required.
   * </ul>
   */
  public int readRawStringHeader() {
    var format = getByte();
    return switch (format) {
      case ValueFormat.STR8 -> getLength8();
      case ValueFormat.STR16 -> getLength16();
      case ValueFormat.STR32 -> getLength32(ValueType.STRING);
      default -> {
        if (ValueFormat.isFixStr(format)) {
          yield ValueFormat.getFixStrLength(format);
        } else {
          throw Exceptions.typeMismatch(format, RequestedType.STRING);
        }
      }
    };
  }

  public ExtensionType.Header readExtensionHeader() {
    var format = getByte();
    return switch (format) {
      case ValueFormat.FIXEXT1 -> new ExtensionType.Header(1, getByte());
      case ValueFormat.FIXEXT2 -> new ExtensionType.Header(2, getByte());
      case ValueFormat.FIXEXT4 -> new ExtensionType.Header(4, getByte());
      case ValueFormat.FIXEXT8 -> new ExtensionType.Header(8, getByte());
      case ValueFormat.FIXEXT16 -> new ExtensionType.Header(16, getByte());
      case ValueFormat.EXT8 -> new ExtensionType.Header(getLength8(), getByte());
      case ValueFormat.EXT16 -> new ExtensionType.Header(getLength16(), getByte());
      case ValueFormat.EXT32 ->
          new ExtensionType.Header(getLength32(ValueType.EXTENSION), getByte());
      default -> throw Exceptions.typeMismatch(format, RequestedType.EXTENSION);
    };
  }

  /**
   * Reads {@linkplain ByteBuffer#remaining() remaining} bytes into the given buffer, starting at
   * the buffer's current {@linkplain ByteBuffer#position() position}.
   *
   * <p>This method is used together with {@link #readBinaryHeader()} or {@link
   * #readRawStringHeader()}.
   */
  public int readPayload(ByteBuffer buffer) {
    return readFromSource(buffer, 1);
  }

  public int readPayload(ByteBuffer buffer, int minBytes) {
    return readFromSource(buffer, minBytes);
  }

  /** Closes the underlying message {@linkplain MessageSource source}. */
  @Override
  public void close() {
    try {
      source.close();
    } catch (IOException e) {
      throw Exceptions.ioErrorClosingSource(e);
    }
  }

  private byte getByte() {
    ensureRemaining(1);
    return buffer.get();
  }

  private short getUByte() {
    ensureRemaining(1);
    return (short) (buffer.get() & 0xff);
  }

  private short getShort() {
    ensureRemaining(2);
    return buffer.getShort();
  }

  private int getUShort() {
    ensureRemaining(2);
    return buffer.getShort() & 0xffff;
  }

  private int getInt() {
    ensureRemaining(4);
    return buffer.getInt();
  }

  private long getUInt() {
    ensureRemaining(4);
    return buffer.getInt() & 0xffffffffL;
  }

  private long getLong() {
    ensureRemaining(8);
    return buffer.getLong();
  }

  private float getFloat() {
    ensureRemaining(4);
    return buffer.getFloat();
  }

  private double getDouble() {
    ensureRemaining(8);
    return buffer.getDouble();
  }

  private int getLength8() {
    ensureRemaining(1);
    return buffer.get() & 0xff;
  }

  private int getLength16() {
    ensureRemaining(2);
    return buffer.getShort() & 0xffff;
  }

  private int getLength32(ValueType valueType) {
    var length = getInt();
    if (length < 0) {
      throw Exceptions.lengthTooLarge(length, valueType);
    }
    return length;
  }

  private int readFromSource(ByteBuffer buffer, int minBytes) {
    try {
      return source.readAtLeast(buffer, minBytes);
    } catch (IOException e) {
      throw Exceptions.ioErrorReadingFromSource(e);
    }
  }

  // non-private for testing
  byte nextFormat() {
    ensureRemaining(1);
    // don't change position
    return buffer.get(buffer.position());
  }

  private void ensureRemaining(int length) {
    ensureRemaining(length, buffer);
  }

  private void ensureRemaining(int length, ByteBuffer buffer) {
    try {
      source.ensureRemaining(length, buffer);
    } catch (IOException e) {
      throw Exceptions.ioErrorReadingFromSource(e);
    }
  }
}
