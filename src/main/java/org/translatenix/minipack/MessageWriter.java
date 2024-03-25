/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.jspecify.annotations.Nullable;
import org.translatenix.minipack.internal.Exceptions;
import org.translatenix.minipack.internal.Utf8StringEncoder;
import org.translatenix.minipack.internal.ValueFormat;

/**
 * Writes values encoded in the <a href="https://msgpack.org/">MessagePack</a> binary serialization
 * format.
 *
 * <p>To create a new {@code MessageWriter}, use a {@linkplain #builder() builder}. To write a
 * value, call one of the {@code write()} or {@code writeXYZ()} methods. To flush the underlying
 * {@linkplain MessageSink sink}, call {@link #flush()}. To close this writer, call {@link
 * #close()}.
 *
 * <p>Unless otherwise noted, methods throw {@link WriterException} if an error occurs. The most
 * common type of error is an I/O error originating from the underlying {@link MessageSink}.
 *
 * @param <T> the parameter type of {@link #writeString} ({@code java.lang.CharSequence} unless this
 *     writer is {@linkplain MessageWriter.Builder#build(StringEncoder) built} with a custom {@link
 *     StringEncoder})
 */
public final class MessageWriter<T> implements Closeable {
  private static final int MIN_BUFFER_CAPACITY = 9;
  private static final int DEFAULT_BUFFER_CAPACITY = 1 << 13;
  private static final int DEFAULT_STRING_SIZE_LIMIT = 1 << 20;

  private final MessageSink sink;
  private final ByteBuffer buffer;
  private final StringEncoder<T> stringEncoder;

  /** A builder of {@code MessageWriter}. */
  public static final class Builder {
    private Builder() {}

    private @Nullable MessageSink sink;
    private @Nullable ByteBuffer buffer;

    /** Sets the message sink to write to. */
    public Builder sink(MessageSink sink) {
      this.sink = sink;
      return this;
    }

    /**
     * Equivalent to {@code sink(MessageSink.of(stream))}.
     *
     * @see MessageSink#of(OutputStream)
     */
    public Builder sink(OutputStream stream) {
      return sink(MessageSink.of(stream));
    }

    /**
     * Equivalent to {@code sink(MessageSink.of(channel))}.
     *
     * @see MessageSink#of(WritableByteChannel)
     */
    public Builder sink(WritableByteChannel channel) {
      return sink(MessageSink.of(channel));
    }

    /**
     * Sets the buffer to use for writing to the underlying message {@linkplain MessageSink sink}.
     *
     * <p>The buffer's {@linkplain ByteBuffer#capacity() capacity} determines the maximum number of
     * bytes that will be written to the sink at once.
     *
     * <p>If not set, defaults to {@code ByteBuffer.allocate(1024 * 8)}.
     */
    public Builder buffer(ByteBuffer buffer) {
      this.buffer = buffer;
      return this;
    }

    /**
     * Equivalent to {@code build(StringEncoder.defaultEncoder(1024 * 1024)}.
     *
     * @see StringEncoder#defaultEncoder(int)
     */
    public MessageWriter<CharSequence> build() {
      return new MessageWriter<>(this, StringEncoder.defaultEncoder(DEFAULT_STRING_SIZE_LIMIT));
    }

    /**
     * Creates a new {@code MessageWriter} from this builder's current state and the given string
     * encoder.
     */
    public <T> MessageWriter<T> build(StringEncoder<T> stringEncoder) {
      return new MessageWriter<>(this, stringEncoder);
    }
  }

  /** Creates a new {@code MessageWriter} builder. */
  public static Builder builder() {
    return new Builder();
  }

  private MessageWriter(Builder builder, StringEncoder<T> stringEncoder) {
    if (builder.sink == null) {
      throw Exceptions.sinkRequired();
    }
    sink = builder.sink;
    buffer =
        builder.buffer != null
            ? builder.buffer.clear()
            : ByteBuffer.allocate(DEFAULT_BUFFER_CAPACITY);
    if (buffer.capacity() < MIN_BUFFER_CAPACITY) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_CAPACITY);
    }
    this.stringEncoder = stringEncoder;
  }

  /** Writes a nil (null) value. */
  public void writeNil() {
    putByte(ValueFormat.NIL);
  }

  /** Writes a boolean value. */
  public void write(boolean value) {
    putByte(value ? ValueFormat.TRUE : ValueFormat.FALSE);
  }

  /** Writes an integer value that fits into a Java byte. */
  public void write(byte value) {
    if (value < -(1 << 5)) {
      putByte(ValueFormat.INT8, value);
    } else {
      putByte(value);
    }
  }

  /** Writes an integer value that fits into a Java short. */
  public void write(short value) {
    if (value < -(1 << 5)) {
      if (value < -(1 << 7)) {
        putShort(ValueFormat.INT16, value);
      } else {
        putByte(ValueFormat.INT8, (byte) value);
      }
    } else if (value < (1 << 7)) {
      putByte((byte) value);
    } else {
      if (value < (1 << 8)) {
        putByte(ValueFormat.UINT8, (byte) value);
      } else {
        putShort(ValueFormat.UINT16, value);
      }
    }
  }

  /** Writes an integer value that fits into a Java int. */
  public void write(int value) {
    if (value < -(1 << 5)) {
      if (value < -(1 << 15)) {
        putInt(ValueFormat.INT32, value);
      } else {
        if (value < -(1 << 7)) {
          putShort(ValueFormat.INT16, (short) value);
        } else {
          putByte(ValueFormat.INT8, (byte) value);
        }
      }
    } else if (value < (1 << 7)) {
      putByte((byte) value);
    } else {
      if (value < (1 << 16)) {
        if (value < (1 << 8)) {
          putByte(ValueFormat.UINT8, (byte) value);
        } else {
          putShort(ValueFormat.UINT16, (short) value);
        }
      } else {
        putInt(ValueFormat.UINT32, value);
      }
    }
  }

  /** Writes an integer value that fits into a Java long. */
  public void write(long value) {
    if (value < -(1L << 5)) {
      if (value < -(1L << 31)) {
        putLong(ValueFormat.INT64, value);
      } else {
        if (value < -(1L << 15)) {
          putInt(ValueFormat.INT32, (int) value);
        } else {
          if (value < -(1L << 7)) {
            putShort(ValueFormat.INT16, (short) value);
          } else {
            putByte(ValueFormat.INT8, (byte) value);
          }
        }
      }
    } else if (value < (1L << 7)) {
      putByte((byte) value);
    } else {
      if (value < (1L << 32)) {
        if (value < (1L << 16)) {
          if (value < (1L << 8)) {
            putByte(ValueFormat.UINT8, (byte) value);
          } else {
            putShort(ValueFormat.UINT16, (short) value);
          }
        } else {
          putInt(ValueFormat.UINT32, (int) value);
        }
      } else {
        putLong(ValueFormat.UINT64, value);
      }
    }
  }

  /** Writes a floating point value that fits into a Java float. */
  public void write(float value) {
    ensureRemaining(5);
    buffer.put(ValueFormat.FLOAT32);
    buffer.putFloat(value);
  }

  /** Writes a floating point value that fits into a Java double. */
  public void write(double value) {
    ensureRemaining(9);
    buffer.put(ValueFormat.FLOAT64);
    buffer.putDouble(value);
  }

  /** Writes a string value. */
  public void writeString(T string) {
    try {
      stringEncoder.encode(string, buffer, sink);
    } catch (IOException e) {
      throw Exceptions.ioErrorWritingToSink(e);
    }
  }

  /**
   * Starts writing an array value with the given number of elements.
   *
   * <p>A call to this method MUST be followed by {@code elementCount} calls that write the array's
   * elements.
   */
  public void writeArrayHeader(int elementCount) {
    requirePositiveLength(elementCount);
    if (elementCount < (1 << 4)) {
      putByte((byte) (ValueFormat.FIXARRAY_PREFIX | elementCount));
    } else if (elementCount < (1 << 16)) {
      putShort(ValueFormat.ARRAY16, (short) elementCount);
    } else {
      putInt(ValueFormat.ARRAY32, elementCount);
    }
  }

  /**
   * Starts writing a map value with the given number of entries.
   *
   * <p>A call to this method MUST be followed by {@code entryCount*2} calls that alternately write
   * the map's keys and values.
   */
  public void writeMapHeader(int entryCount) {
    requirePositiveLength(entryCount);
    if (entryCount < (1 << 4)) {
      putByte((byte) (ValueFormat.FIXMAP_PREFIX | entryCount));
    } else if (entryCount < (1 << 16)) {
      putShort(ValueFormat.MAP16, (short) entryCount);
    } else {
      putInt(ValueFormat.MAP32, entryCount);
    }
  }

  /**
   * Starts writing a string value with the given number of UTF-8 bytes.
   *
   * <p>A call to this method MUST be followed by one or more calls to {@link #writePayload} that
   * write exactly {@code byteCount} bytes in total.
   *
   * <p>This method is a low-level alternative to {@link #writeString}. It can be useful in the
   * following cases:
   *
   * <ul>
   *   <li>The string to write is already available as UTF-8 byte sequence.
   *   <li>Full control over conversion from {@code java.lang.String} to UTF-8 is required.
   * </ul>
   */
  public void writeRawStringHeader(int byteCount) {
    requirePositiveLength(byteCount);
    try {
      Utf8StringEncoder.encodeHeader(byteCount, buffer, sink);
    } catch (IOException e) {
      throw Exceptions.ioErrorWritingToSink(e);
    }
  }

  /**
   * Starts writing a binary value with the given number of bytes.
   *
   * <p>A call to this method MUST be followed by one or more calls to {@link #writePayload} that
   * write exactly {@code byteCount} bytes in total.
   */
  public void writeBinaryHeader(int byteCount) {
    requirePositiveLength(byteCount);
    if (byteCount < (1 << 8)) {
      putByte(ValueFormat.BIN8, (byte) byteCount);
    } else if (byteCount < (1 << 16)) {
      putShort(ValueFormat.BIN16, (short) byteCount);
    } else {
      putInt(ValueFormat.BIN32, byteCount);
    }
  }

  public void writeExtensionHeader(int byteCount, byte type) {
    requirePositiveLength(byteCount);
    switch (byteCount) {
      case 1 -> putByte(ValueFormat.FIXEXT1, type);
      case 2 -> putByte(ValueFormat.FIXEXT2, type);
      case 4 -> putByte(ValueFormat.FIXEXT4, type);
      case 8 -> putByte(ValueFormat.FIXEXT8, type);
      case 16 -> putByte(ValueFormat.FIXEXT16, type);
      default -> {
        if (byteCount < (1 << 8)) {
          putByte(ValueFormat.EXT8, (byte) byteCount);
        } else if (byteCount < (1 << 16)) {
          putShort(ValueFormat.EXT16, (short) byteCount);
        } else {
          putInt(ValueFormat.EXT32, byteCount);
        }
        putByte(type);
      }
    }
  }

  /**
   * Writes the given buffer's {@linkplain ByteBuffer#remaining() length} bytes, starting at the
   * buffer's current {@linkplain ByteBuffer#position() position}.
   *
   * <p>This method must be called after {@link #writeBinaryHeader} or {@link
   * #writeRawStringHeader}.
   */
  public int writePayload(ByteBuffer buffer) {
    writeBuffer();
    return writeToSink(buffer);
  }

  /**
   * Writes any data remaining in this writer's buffer and flushes the underlying message
   * {@linkplain MessageSink sink}.
   */
  public void flush() {
    writeBuffer();
    try {
      sink.flush();
    } catch (IOException e) {
      throw Exceptions.ioErrorFlushingSink(e);
    }
  }

  /**
   * {@linkplain MessageSink#close() Closes} the underlying message {@linkplain MessageSink sink}.
   */
  @Override
  public void close() {
    try {
      sink.close();
    } catch (IOException e) {
      throw Exceptions.ioErrorClosingSink(e);
    }
  }

  private void requirePositiveLength(int length) {
    if (length < 0) throw Exceptions.negativeLength(length);
  }

  private void putByte(byte value) {
    ensureRemaining(1);
    buffer.put(value);
  }

  private void putByte(byte format, byte value) {
    ensureRemaining(2);
    buffer.put(format);
    buffer.put(value);
  }

  private void putShort(byte format, short value) {
    ensureRemaining(3);
    buffer.put(format);
    buffer.putShort(value);
  }

  private void putInt(byte format, int value) {
    ensureRemaining(5);
    buffer.put(format);
    buffer.putInt(value);
  }

  private void putLong(byte format, long value) {
    ensureRemaining(9);
    buffer.put(format);
    buffer.putLong(value);
  }

  private void ensureRemaining(int byteCount) {
    try {
      sink.ensureRemaining(buffer, byteCount);
    } catch (IOException e) {
      throw Exceptions.ioErrorWritingToSink(e);
    }
  }

  private void writeBuffer() {
    buffer.flip();
    writeToSink(buffer);
    buffer.clear();
  }

  private int writeToSink(ByteBuffer buffer) {
    try {
      return sink.write(buffer);
    } catch (IOException e) {
      throw Exceptions.ioErrorWritingToSink(e);
    }
  }
}
