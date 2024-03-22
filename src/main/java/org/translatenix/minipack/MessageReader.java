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
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.Nullable;

/**
 * Reads messages encoded in the <a href="https://msgpack.org/">MessagePack</a> binary serialization
 * format.
 *
 * <p>To create a new {@code MessageReader}, use a {@linkplain #builder() builder}. To read a
 * message, call one of the {@code readXYZ()} methods. To peek at the next message type, call {@link
 * #nextType()}. If an error occurs when reading a value, a {@link ReaderException} is thrown.
 */
public final class MessageReader implements Closeable {
  private static final int MIN_BUFFER_CAPACITY = 9;
  private static final int DEFAULT_BUFFER_CAPACITY = 1 << 13;
  private static final int DEFAULT_MAX_ALLOCATOR_CAPACITY = 1 << 20;

  private final MessageSource source;
  private final ByteBuffer buffer;
  private final BufferAllocator allocator;

  /** A builder of {@link MessageReader}. */
  public static final class Builder {
    private @Nullable MessageSource source;
    private @Nullable ByteBuffer buffer;
    private @Nullable BufferAllocator allocator;
    private int maxAllocatorCapacity = DEFAULT_MAX_ALLOCATOR_CAPACITY;

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

    /**
     * Sets the allocator to use for allocating additional {@linkplain ByteBuffer byte buffers}.
     *
     * <p>Currently, an additional byte buffer is only allocated if {@link #readString()} is called
     * and at least one of the following conditions holds:
     *
     * <ul>
     *   <li>The string is too large to fit into the regular {@linkplain #buffer(ByteBuffer) buffer}
     *       or a previously allocated additional buffer.
     *   <li>The regular {@linkplain #buffer(ByteBuffer) buffer} is not backed by an accessible
     *       {@linkplain ByteBuffer#array() array}.
     * </ul>
     */
    public Builder allocator(BufferAllocator allocator) {
      this.allocator = allocator;
      return this;
    }

    /**
     * Shorthand for {@code allocator(BufferAllocator.withCapacity(buffer.capacity() * 2,
     * maxCapacity))}, where {@code buffer} is the meassage reader's regular {@linkplain
     * #buffer(ByteBuffer) buffer}.
     *
     * @see #allocator(BufferAllocator)
     */
    public Builder allocatorCapacity(int maxCapacity) {
      maxAllocatorCapacity = maxCapacity;
      return this;
    }

    /** Creates a new {@code MessageReader} from this builder's current state. */
    public MessageReader build() {
      return new MessageReader(this);
    }
  }

  /** Creates a new {@code MessageWriter} builder. */
  public static Builder builder() {
    return new Builder();
  }

  private MessageReader(Builder builder) {
    if (builder.source == null) {
      throw Exceptions.sourceRequired();
    }
    this.source = builder.source;
    this.buffer =
        builder.buffer != null
            ? builder.buffer.position(0).limit(0)
            : ByteBuffer.allocate(DEFAULT_BUFFER_CAPACITY).limit(0);
    assert this.buffer.remaining() == 0;
    if (buffer.capacity() < MIN_BUFFER_CAPACITY) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_CAPACITY);
    }
    this.allocator =
        builder.allocator != null
            ? builder.allocator
            : BufferAllocator.withCapacity(
                Math.min(builder.maxAllocatorCapacity, buffer.capacity() * 2),
                builder.maxAllocatorCapacity);
  }

  // TODO handle EOF
  /** Returns the type of the next value to be read. */
  public ValueType nextType() {
    ensureRemaining(1);
    // don't change position
    return ValueFormat.toType(buffer.get(buffer.position()));
  }

  /** Reads a nil (null) value. */
  public void readNil() {
    ensureRemaining(1);
    var format = buffer.get();
    if (format != ValueFormat.NIL) {
      throw Exceptions.wrongJavaType(format, JavaType.VOID);
    }
  }

  /** Reads a boolean value. */
  public boolean readBoolean() {
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.TRUE -> true;
      case ValueFormat.FALSE -> false;
      default -> throw Exceptions.wrongJavaType(format, JavaType.BOOLEAN);
    };
  }

  /** Reads an integer value that fits into a Java byte. */
  public byte readByte() {
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.INT8 -> {
        ensureRemaining(1);
        yield buffer.get();
      }
      case ValueFormat.INT16 -> {
        ensureRemaining(2);
        var value = buffer.getShort();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, format, JavaType.BYTE);
      }
      case ValueFormat.INT32 -> {
        ensureRemaining(4);
        var value = buffer.getInt();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, format, JavaType.BYTE);
      }
      case ValueFormat.INT64 -> {
        ensureRemaining(8);
        var value = buffer.getLong();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, format, JavaType.BYTE);
      }
      case ValueFormat.UINT8 -> {
        ensureRemaining(1);
        var value = buffer.get();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, format, JavaType.BYTE);
      }
      case ValueFormat.UINT16 -> {
        ensureRemaining(2);
        var value = buffer.getShort();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, format, JavaType.BYTE);
      }
      case ValueFormat.UINT32 -> {
        ensureRemaining(4);
        var value = buffer.getInt();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, format, JavaType.BYTE);
      }
      case ValueFormat.UINT64 -> {
        ensureRemaining(8);
        var value = buffer.getLong();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, format, JavaType.BYTE);
      }
      default -> {
        if (ValueFormat.isFixInt(format)) yield format;
        throw Exceptions.wrongJavaType(format, JavaType.BYTE);
      }
    };
  }

  /** Reads an integer value that fits into a Java short. */
  public short readShort() {
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.INT8 -> {
        ensureRemaining(1);
        yield buffer.get();
      }
      case ValueFormat.INT16 -> {
        ensureRemaining(2);
        yield buffer.getShort();
      }
      case ValueFormat.INT32 -> {
        ensureRemaining(4);
        var value = buffer.getInt();
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, format, JavaType.SHORT);
      }
      case ValueFormat.INT64 -> {
        ensureRemaining(8);
        var value = buffer.getLong();
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, format, JavaType.SHORT);
      }
      case ValueFormat.UINT8 -> {
        ensureRemaining(1);
        yield (short) (buffer.get() & 0xff);
      }
      case ValueFormat.UINT16 -> {
        ensureRemaining(2);
        var value = buffer.getShort();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, format, JavaType.SHORT);
      }
      case ValueFormat.UINT32 -> {
        ensureRemaining(4);
        var value = buffer.getInt();
        if (value >= 0 && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, format, JavaType.SHORT);
      }
      case ValueFormat.UINT64 -> {
        ensureRemaining(8);
        var value = buffer.getLong();
        if (value >= 0 && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, format, JavaType.SHORT);
      }
      default -> {
        if (ValueFormat.isFixInt(format)) yield format;
        throw Exceptions.wrongJavaType(format, JavaType.SHORT);
      }
    };
  }

  /** Reads an integer value that fits into a Java int. */
  public int readInt() {
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.INT8 -> {
        ensureRemaining(1);
        yield buffer.get();
      }
      case ValueFormat.INT16 -> {
        ensureRemaining(2);
        yield buffer.getShort();
      }
      case ValueFormat.INT32 -> {
        ensureRemaining(4);
        yield buffer.getInt();
      }
      case ValueFormat.INT64 -> {
        ensureRemaining(8);
        var value = buffer.getLong();
        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) yield (int) value;
        throw Exceptions.integerOverflow(value, format, JavaType.INT);
      }
      case ValueFormat.UINT8 -> {
        ensureRemaining(1);
        yield buffer.get() & 0xff;
      }
      case ValueFormat.UINT16 -> {
        ensureRemaining(2);
        yield buffer.getShort() & 0xffff;
      }
      case ValueFormat.UINT32 -> {
        ensureRemaining(4);
        var value = buffer.getInt();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, format, JavaType.INT);
      }
      case ValueFormat.UINT64 -> {
        ensureRemaining(8);
        var value = buffer.getLong();
        if (value >= 0 && value <= Integer.MAX_VALUE) yield (int) value;
        throw Exceptions.integerOverflow(value, format, JavaType.INT);
      }
      default -> {
        if (ValueFormat.isFixInt(format)) yield format;
        throw Exceptions.wrongJavaType(format, JavaType.INT);
      }
    };
  }

  /** Reads an integer value that fits into a Java long. */
  public long readLong() {
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.INT8 -> {
        ensureRemaining(1);
        yield buffer.get();
      }
      case ValueFormat.INT16 -> {
        ensureRemaining(2);
        yield buffer.getShort();
      }
      case ValueFormat.INT32 -> {
        ensureRemaining(4);
        yield buffer.getInt();
      }
      case ValueFormat.INT64 -> {
        ensureRemaining(8);
        yield buffer.getLong();
      }
      case ValueFormat.UINT8 -> {
        ensureRemaining(1);
        yield buffer.get() & 0xff;
      }
      case ValueFormat.UINT16 -> {
        ensureRemaining(2);
        yield buffer.getShort() & 0xffff;
      }
      case ValueFormat.UINT32 -> {
        ensureRemaining(4);
        yield buffer.getInt() & 0xffffffffL;
      }
      case ValueFormat.UINT64 -> {
        ensureRemaining(8);
        var value = buffer.getLong();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, format, JavaType.LONG);
      }
      default -> {
        if (ValueFormat.isFixInt(format)) yield format;
        throw Exceptions.wrongJavaType(format, JavaType.INT);
      }
    };
  }

  /** Reads a floating point value that fits into a Java float. */
  public float readFloat() {
    ensureRemaining(1);
    var format = buffer.get();
    if (format == ValueFormat.FLOAT32) {
      ensureRemaining(4);
      return buffer.getFloat();
    }
    throw Exceptions.wrongJavaType(format, JavaType.FLOAT);
  }

  /** Reads a floating point value that fits into a Java double. */
  public double readDouble() {
    ensureRemaining(1);
    var format = buffer.get();
    if (format == ValueFormat.FLOAT64) {
      ensureRemaining(8);
      return buffer.getDouble();
    }
    throw Exceptions.wrongJavaType(format, JavaType.DOUBLE);
  }

  /**
   * Reads a string value.
   *
   * <p>The maximum UTF-8 string length is determined by the {@link BufferAllocator} that this
   * reader was built with. The default maximum UTF-8 string length is 1 MiB (1024 * 1024 bytes).
   *
   * <p>For a lower-level way to read strings, see {@link #readRawStringHeader()}.
   */
  public String readString() {
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.STR8 -> {
        ensureRemaining(1);
        yield readString(buffer.get() & 0xff);
      }
      case ValueFormat.STR16 -> {
        ensureRemaining(2);
        yield readString(buffer.getShort() & 0xffff);
      }
      case ValueFormat.STR32 -> {
        ensureRemaining(4);
        yield readString(buffer.getInt());
      }
      default -> {
        if (ValueFormat.isFixStr(format)) {
          yield readString(ValueFormat.getFixStrLength(format));
        } else {
          throw Exceptions.wrongJavaType(format, JavaType.STRING);
        }
      }
    };
  }

  /**
   * Starts writing an array value.
   *
   * <p>A call to this method MUST be followed by {@code n} calls that read the array's elements,
   * where {@code n} is the number of array elements returned by this method.
   */
  public int readArrayHeader() {
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.ARRAY16 -> {
        ensureRemaining(2);
        yield buffer.getShort();
      }
      case ValueFormat.ARRAY32 -> {
        ensureRemaining(4);
        yield buffer.getInt();
      }
      default -> {
        if (ValueFormat.isFixArray(format)) {
          yield ValueFormat.getFixArrayLength(format);
        }
        throw Exceptions.wrongJavaType(format, JavaType.MAP);
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
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.MAP16 -> {
        ensureRemaining(2);
        yield buffer.getShort();
      }
      case ValueFormat.MAP32 -> {
        ensureRemaining(4);
        yield buffer.getInt();
      }
      default -> {
        if (ValueFormat.isFixMap(format)) {
          yield ValueFormat.getFixMapLength(format);
        }
        throw Exceptions.wrongJavaType(format, JavaType.MAP);
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
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.BIN8 -> {
        ensureRemaining(1);
        yield buffer.get() & 0xff;
      }
      case ValueFormat.BIN16 -> {
        ensureRemaining(2);
        yield buffer.getShort() & 0xffff;
      }
      case ValueFormat.BIN32 -> {
        ensureRemaining(4);
        var result = buffer.getInt();
        if (result < 0) throw Exceptions.binaryTooLarge(result);
        yield result;
      }
      default -> throw Exceptions.wrongJavaType(format, JavaType.STRING);
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
    ensureRemaining(1);
    var format = buffer.get();
    return switch (format) {
      case ValueFormat.STR8 -> {
        ensureRemaining(1);
        yield buffer.get() & 0xff;
      }
      case ValueFormat.STR16 -> {
        ensureRemaining(2);
        yield buffer.getShort() & 0xffff;
      }
      case ValueFormat.STR32 -> {
        ensureRemaining(4);
        yield buffer.getInt();
      }
      default -> {
        if (ValueFormat.isFixStr(format)) {
          yield ValueFormat.getFixStrLength(format);
        } else {
          throw Exceptions.wrongJavaType(format, JavaType.STRING);
        }
      }
    };
  }

  /**
   * Reads {@linkplain ByteBuffer#remaining() remaining} bytes into the given buffer, starting at
   * the buffer's current {@linkplain ByteBuffer#position() position}.
   *
   * <p>This method is used together with {@link #readBinaryHeader()} or {@link
   * #readRawStringHeader()}.
   */
  public void readPayload(ByteBuffer buffer) {
    readFromSource(buffer, buffer.remaining());
  }

  /**
   * Reads between {@code minBytes} and {@linkplain ByteBuffer#remaining() remaining} bytes into the
   * given buffer, starting at the buffer's current {@linkplain ByteBuffer#position() position}.
   *
   * <p>This method is used together with {@link #readBinaryHeader()} or {@link
   * #readRawStringHeader()}.
   */
  public void readPayload(ByteBuffer buffer, int minBytes) {
    readFromSource(buffer, minBytes);
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

  private void readFromSource(ByteBuffer buffer, int minBytes) {
    assert minBytes <= buffer.remaining();
    try {
      source.read(buffer, minBytes);
    } catch (IOException e) {
      throw Exceptions.ioErrorReadingFromSource(e);
    }
  }

  // for testing only
  byte nextFormat() {
    ensureRemaining(1);
    // don't change position
    return buffer.get(buffer.position());
  }

  private String readString(int length) {
    if (length < 0) {
      throw Exceptions.stringTooLarge(length & 0xffffffffL, Integer.MAX_VALUE);
    }
    if (length <= buffer.capacity() && buffer.hasArray()) {
      ensureRemaining(length, buffer);
      var result = convertToString(buffer, length);
      buffer.position(buffer.position() + length);
      return result;
    }
    var tempBuffer = allocator.getArrayBackedBuffer(length).position(0).limit(length);
    var transferLength = Math.min(length, buffer.remaining());
    tempBuffer.put(0, buffer, buffer.position(), transferLength);
    if (transferLength < length) {
      tempBuffer.position(transferLength);
      readFromSource(tempBuffer, tempBuffer.remaining());
      tempBuffer.position(0);
    }
    buffer.position(buffer.position() + transferLength);
    return convertToString(tempBuffer, length);
  }

  private String convertToString(ByteBuffer buffer, int length) {
    assert buffer.hasArray();
    return new String(
        buffer.array(), buffer.arrayOffset() + buffer.position(), length, StandardCharsets.UTF_8);
  }

  private void ensureRemaining(int length) {
    ensureRemaining(length, buffer);
  }

  private void ensureRemaining(int length, ByteBuffer buffer) {
    int minBytes = length - buffer.remaining();
    if (minBytes > 0) {
      buffer.compact();
      readFromSource(buffer, minBytes);
      buffer.flip();
      assert buffer.remaining() >= length;
    }
  }
}
