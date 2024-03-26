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
import java.util.HashMap;
import java.util.Map;
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
  private static final int MIN_BUFFER_CAPACITY = 9;
  private static final int DEFAULT_BUFFER_CAPACITY = 1 << 13;
  private static final int DEFAULT_STRING_SIZE_LIMIT = 1 << 20;
  private static final int DEFAULT_IDENTIFIER_CACHE_LIMIT = 1 << 10;

  private final MessageSource source;
  private final ByteBuffer buffer;
  private final Decoder<String> stringDecoder;
  private final Decoder<String> identifierDecoder;
  private final Map<Class<?>, Decoder<?>> valueDecoders;

  /** A builder of {@link MessageReader}. */
  public static final class Builder {
    private @Nullable MessageSource source;
    private @Nullable ByteBuffer buffer;
    private Decoder<String> stringDecoder = Decoder.defaultStringDecoder(DEFAULT_STRING_SIZE_LIMIT);
    private Decoder<String> identifierDecoder =
        Decoder.defaultIdentifierDecoder(DEFAULT_IDENTIFIER_CACHE_LIMIT);
    private final Map<Class<?>, Decoder<?>> valueDecoders = new HashMap<>();

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

    public <T> Builder valueDecoder(Class<T> type, Decoder<? extends T> decoder) {
      valueDecoders.put(type, decoder);
      return this;
    }

    /**
     * Equivalent to {@code build(Decoder.defaultStringDecoder(1024 * 1024),
     * Decoder.defaultIdentifierDecoder(1024))}.
     *
     * @see Decoder#defaultStringDecoder(int)
     * @see Decoder#defaultIdentifierDecoder(int)
     */
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
            : ByteBuffer.allocate(DEFAULT_BUFFER_CAPACITY).limit(0);
    if (buffer.capacity() < MIN_BUFFER_CAPACITY) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_CAPACITY);
    }
    stringDecoder = builder.stringDecoder;
    identifierDecoder = builder.identifierDecoder;
    valueDecoders = Map.copyOf(builder.valueDecoders);
  }

  /** Returns the type of the next value to be read. */
  public ValueType nextType() throws IOException {
    return ValueFormat.toType(source.nextByte(buffer));
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
    source.ensureRemaining(5, buffer);
    var format = buffer.get();
    if (format == ValueFormat.FLOAT32) return buffer.getFloat();
    throw Exceptions.typeMismatch(format, RequestedType.FLOAT);
  }

  /** Reads a floating point value that fits into a Java double. */
  public double readDouble() throws IOException {
    source.ensureRemaining(9, buffer);
    var format = buffer.get();
    if (format == ValueFormat.FLOAT64) return buffer.getDouble();
    throw Exceptions.typeMismatch(format, RequestedType.DOUBLE);
  }

  /**
   * Reads a string value.
   *
   * <p>To read a string as a sequence of bytes, use {@link #readRawStringHeader()} together with
   * {@link #readPayload}.
   */
  public String readString() throws IOException {
    return stringDecoder.decode(buffer, source);
  }

  public String readIdentifier() throws IOException {
    return identifierDecoder.decode(buffer, source);
  }

  public <T> T readValue(Class<T> type) throws IOException {
    @SuppressWarnings("unchecked")
    var decoder = (Decoder<T>) valueDecoders.get(type);
    if (decoder == null) {
      throw Exceptions.unknownValueType(type);
    }
    return decoder.decode(buffer, source);
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
   * <p>A call to this method <i>must</i> be followed by one or multiple calls to {@link
   * #readPayload} that in total read <i>exactly</i> the number of bytes returned by this method.
   */
  public int readBinaryHeader() throws IOException {
    return source.getBinaryHeader(buffer);
  }

  /**
   * Starts reading a string value as a sequence of bytes.
   *
   * <p>A call to this method <i>must</i> be followed by one or more calls to {@link #readPayload}
   * that in total read <i>exactly</i> the number of bytes returned by this method.
   *
   * <p>This method is a low-level alternative to {@link #readString()}.
   */
  public int readRawStringHeader() throws IOException {
    return source.getStringHeader(buffer);
  }

  /**
   * Starts reading an extension value.
   *
   * <p>A call to this method <i>must</i> be followed by one or multiple calls to {@link
   * #readPayload} that in total read <i>exactly</i> the number of bytes stated in the returned
   * {@code Header}.
   */
  public ExtensionType.Header readExtensionHeader() throws IOException {
    return source.getExtensionHeader(buffer);
  }

  /**
   * Equivalent to {@code readPayload(buffer, 1)}.
   *
   * @see #readPayload(ByteBuffer, int)
   */
  public int readPayload(ByteBuffer buffer) throws IOException {
    return source.readAtLeast(buffer, 1);
  }

  /**
   * Reads between {@code minBytes} and {@linkplain ByteBuffer#remaining() remaining} bytes into the
   * given buffer, starting at the buffer's current {@linkplain ByteBuffer#position() position}.
   *
   * <p>This method is meant to be called one or multiple times after {@link #readBinaryHeader()} or
   * {@link #readRawStringHeader()}.
   */
  public int readPayload(ByteBuffer buffer, int minBytes) throws IOException {
    return source.readAtLeast(buffer, minBytes);
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

  private short getUByte() throws IOException {
    return source.getUByte(buffer);
  }

  private int getUShort() throws IOException {
    return source.getUShort(buffer);
  }

  private long getUInt() throws IOException {
    return source.getUInt(buffer);
  }

  private int getLength8() throws IOException {
    return source.getLength8(buffer);
  }

  private int getLength16() throws IOException {
    return source.getLength16(buffer);
  }

  private int getLength32(ValueType valueType) throws IOException {
    return source.getLength32(buffer, valueType);
  }

  // non-private for testing
  byte nextFormat() throws IOException {
    return source.nextByte(buffer);
  }
}
