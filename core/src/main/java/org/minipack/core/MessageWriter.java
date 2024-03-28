/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.minipack.core.internal.Exceptions;
import org.minipack.core.internal.ValueFormat;

/**
 * Writes values encoded in the <a href="https://msgpack.org/">MessagePack</a> binary serialization
 * format.
 *
 * <p>To create a new {@code MessageWriter}, use a {@linkplain #builder() builder}. To write a
 * value, call one of the {@code write()} or {@code writeXYZ()} methods. To flush the underlying
 * {@linkplain MessageSink sink}, call {@link #flush()}. To close this writer, call {@link
 * #close()}.
 *
 * <p>Unless otherwise noted, methods throw {@link IOException} if an I/O error occurs, and {@link
 * WriterException} if some other error occurs.
 */
public final class MessageWriter implements Closeable {
  private static final int MIN_BUFFER_SIZE = 9;
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
  private static final int MAX_STRING_SIZE = 1024 * 1024;
  private static final int MAX_IDENTIFIER_CACHE_SIZE = 1024 * 1024; // in bytes
  private static final byte TIMESTAMP_EXTENSION_TYPE = -1;

  private final MessageSink sink;
  private final ByteBuffer buffer;
  private final Encoder<CharSequence> stringEncoder;
  private final Encoder<String> identifierEncoder;

  /** A builder of {@code MessageWriter}. */
  public static final class Builder {
    private Builder() {}

    private @Nullable MessageSink sink;
    private @Nullable ByteBuffer buffer;
    private Encoder<CharSequence> stringEncoder = Encoder.stringEncoder(MAX_STRING_SIZE);
    private Encoder<String> identifierEncoder =
        Encoder.identifierEncoder(MAX_IDENTIFIER_CACHE_SIZE);

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

    public Builder stringEncoder(Encoder<CharSequence> encoder) {
      stringEncoder = encoder;
      return this;
    }

    public Builder identifierEncoder(Encoder<String> encoder) {
      identifierEncoder = encoder;
      return this;
    }

    /** Creates a new {@code MessageWriter} from this builder's current state. */
    public MessageWriter build() {
      return new MessageWriter(this);
    }
  }

  /** Creates a new {@code MessageWriter} builder. */
  public static Builder builder() {
    return new Builder();
  }

  private MessageWriter(Builder builder) {
    if (builder.sink == null) {
      throw Exceptions.sinkRequired();
    }
    sink = builder.sink;
    buffer =
        builder.buffer != null ? builder.buffer.clear() : ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    if (buffer.capacity() < MIN_BUFFER_SIZE) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_SIZE);
    }
    stringEncoder = builder.stringEncoder;
    identifierEncoder = builder.identifierEncoder;
  }

  /** Writes a nil (null) value. */
  public void writeNil() throws IOException {
    putByte(ValueFormat.NIL);
  }

  /** Writes a boolean value. */
  public void write(boolean value) throws IOException {
    putByte(value ? ValueFormat.TRUE : ValueFormat.FALSE);
  }

  /** Writes an integer value that fits into a Java byte. */
  public void write(byte value) throws IOException {
    if (value < -(1 << 5)) {
      putInt8(value);
    } else {
      putByte(value);
    }
  }

  /** Writes an integer value that fits into a Java short. */
  public void write(short value) throws IOException {
    if (value < -(1 << 5)) {
      if (value < -(1 << 7)) {
        putInt16(value);
      } else {
        putInt8((byte) value);
      }
    } else if (value < (1 << 7)) {
      putByte((byte) value);
    } else {
      if (value < (1 << 8)) {
        putUInt8((byte) value);
      } else {
        putUInt16(value);
      }
    }
  }

  /** Writes an integer value that fits into a Java int. */
  public void write(int value) throws IOException {
    if (value < -(1 << 5)) {
      if (value < -(1 << 15)) {
        putInt32(value);
      } else {
        if (value < -(1 << 7)) {
          putInt16((short) value);
        } else {
          putInt8((byte) value);
        }
      }
    } else if (value < (1 << 7)) {
      putByte((byte) value);
    } else {
      if (value < (1 << 16)) {
        if (value < (1 << 8)) {
          putUInt8((byte) value);
        } else {
          putUInt16((short) value);
        }
      } else {
        putUInt32(value);
      }
    }
  }

  /** Writes an integer value that fits into a Java long. */
  public void write(long value) throws IOException {
    if (value < -(1L << 5)) {
      if (value < -(1L << 31)) {
        putInt64(value);
      } else {
        if (value < -(1L << 15)) {
          putInt32((int) value);
        } else {
          if (value < -(1L << 7)) {
            putInt16((short) value);
          } else {
            putInt8((byte) value);
          }
        }
      }
    } else if (value < (1L << 7)) {
      putByte((byte) value);
    } else {
      if (value < (1L << 32)) {
        if (value < (1L << 16)) {
          if (value < (1L << 8)) {
            putUInt8((byte) value);
          } else {
            putUInt16((short) value);
          }
        } else {
          putUInt32((int) value);
        }
      } else {
        putUInt64(value);
      }
    }
  }

  /** Writes a floating point value that fits into a Java float. */
  public void write(float value) throws IOException {
    putFloat32(value);
  }

  /** Writes a floating point value that fits into a Java double. */
  public void write(double value) throws IOException {
    putFloat64(value);
  }

  /** Writes a timestamp value. */
  public void write(Instant value) throws IOException {
    var seconds = value.getEpochSecond();
    var nanos = value.getNano();
    if (nanos == 0 && seconds >= 0 && seconds < (1L << 32)) {
      writeExtensionHeader(4, TIMESTAMP_EXTENSION_TYPE);
      putInt((int) seconds);
    } else if (seconds >= 0 && seconds < (1L << 34)) {
      writeExtensionHeader(8, TIMESTAMP_EXTENSION_TYPE);
      putLong(((long) nanos) << 34 | seconds);
    } else {
      writeExtensionHeader(12, TIMESTAMP_EXTENSION_TYPE);
      putInt(nanos);
      putLong(seconds);
    }
  }

  /** Writes a string value. */
  public void write(CharSequence string) throws IOException {
    stringEncoder.encode(string, buffer, sink, this);
  }

  /** Writes an identifier value. */
  public void writeIdentifier(String identifier) throws IOException {
    identifierEncoder.encode(identifier, buffer, sink, this);
  }

  public <T> void write(T value, Encoder<T> encoder) throws IOException {
    encoder.encode(value, buffer, sink, this);
  }

  /**
   * Starts writing an array value with the given number of elements.
   *
   * <p>A call to this method MUST be followed by {@code elementCount} calls that write the array's
   * elements.
   */
  public void writeArrayHeader(int elementCount) throws IOException {
    requirePositiveLength(elementCount);
    if (elementCount < (1 << 4)) {
      putByte((byte) (ValueFormat.FIXARRAY_PREFIX | elementCount));
    } else if (elementCount < (1 << 16)) {
      putByteAndShort(ValueFormat.ARRAY16, (short) elementCount);
    } else {
      putByteAndInt(ValueFormat.ARRAY32, elementCount);
    }
  }

  /**
   * Starts writing a map value with the given number of entries.
   *
   * <p>A call to this method MUST be followed by {@code entryCount*2} calls that alternately write
   * the map's keys and values.
   */
  public void writeMapHeader(int entryCount) throws IOException {
    requirePositiveLength(entryCount);
    if (entryCount < (1 << 4)) {
      putByte((byte) (ValueFormat.FIXMAP_PREFIX | entryCount));
    } else if (entryCount < (1 << 16)) {
      putByteAndShort(ValueFormat.MAP16, (short) entryCount);
    } else {
      putByteAndInt(ValueFormat.MAP32, entryCount);
    }
  }

  /**
   * Starts writing a string value with the given number of UTF-8 bytes.
   *
   * <p>A call to this method MUST be followed by one or more calls to {@link #writePayload} that
   * write exactly {@code byteCount} bytes in total.
   *
   * <p>This method is a low-level alternative to {@link #write}. It can be useful in the following
   * cases:
   *
   * <ul>
   *   <li>The string to write is already available as UTF-8 byte sequence.
   *   <li>Full control over conversion from {@code java.lang.String} to UTF-8 is required.
   * </ul>
   */
  public void writeStringHeader(int length) throws IOException {
    if (length < 0) {
      throw Exceptions.negativeLength(length);
    }
    if (length < (1 << 5)) {
      putByte((byte) (ValueFormat.FIXSTR_PREFIX | length));
    } else if (length < (1 << 8)) {
      putBytes(ValueFormat.STR8, (byte) length);
    } else if (length < (1 << 16)) {
      putByteAndShort(ValueFormat.STR16, (short) length);
    } else {
      putByteAndInt(ValueFormat.STR32, length);
    }
  }

  /**
   * Starts writing a binary value with the given number of bytes.
   *
   * <p>A call to this method MUST be followed by one or more calls to {@link #writePayload} that
   * write exactly {@code byteCount} bytes in total.
   */
  public void writeBinaryHeader(int length) throws IOException {
    if (length < 0) {
      throw Exceptions.negativeLength(length);
    }
    if (length < (1 << 8)) {
      putBytes(ValueFormat.BIN8, (byte) length);
    } else if (length < (1 << 16)) {
      putByteAndShort(ValueFormat.BIN16, (short) length);
    } else {
      putByteAndInt(ValueFormat.BIN32, length);
    }
  }

  public void writeExtensionHeader(int length, byte type) throws IOException {
    if (length < 0) {
      throw Exceptions.negativeLength(length);
    }
    switch (length) {
      case 1 -> putBytes(ValueFormat.FIXEXT1, type);
      case 2 -> putBytes(ValueFormat.FIXEXT2, type);
      case 4 -> putBytes(ValueFormat.FIXEXT4, type);
      case 8 -> putBytes(ValueFormat.FIXEXT8, type);
      case 16 -> putBytes(ValueFormat.FIXEXT16, type);
      default -> {
        if (length < (1 << 8)) {
          putBytes(ValueFormat.EXT8, (byte) length);
        } else if (length < (1 << 16)) {
          putByteAndShort(ValueFormat.EXT16, (short) length);
        } else {
          putByteAndInt(ValueFormat.EXT32, length);
        }
        putByte(type);
      }
    }
  }

  public void writePayload(ByteBuffer buffer) throws IOException {
    writeBuffer();
    sink.write(buffer);
  }

  /**
   * Writes any data remaining in this writer's buffer and flushes the underlying message
   * {@linkplain MessageSink sink}.
   */
  public void flush() throws IOException {
    writeBuffer();
    sink.flush();
  }

  /**
   * {@linkplain MessageSink#close() Closes} the underlying message {@linkplain MessageSink sink}.
   */
  @Override
  public void close() throws IOException {
    sink.close();
  }

  private void requirePositiveLength(int length) {
    if (length < 0) throw Exceptions.negativeLength(length);
  }

  private void putByte(byte value) throws IOException {
    sink.putByte(buffer, value);
  }

  private void putBytes(byte value1, byte value2) throws IOException {
    sink.putBytes(buffer, value1, value2);
  }

  private void putInt(int value) throws IOException {
    sink.putInt(buffer, value);
  }

  private void putLong(long value) throws IOException {
    sink.putLong(buffer, value);
  }

  private void putInt8(byte value) throws IOException {
    putBytes(ValueFormat.INT8, value);
  }

  private void putUInt8(byte value) throws IOException {
    putBytes(ValueFormat.UINT8, value);
  }

  private void putInt16(short value) throws IOException {
    putByteAndShort(ValueFormat.INT16, value);
  }

  private void putUInt16(short value) throws IOException {
    putByteAndShort(ValueFormat.UINT16, value);
  }

  private void putInt32(int value) throws IOException {
    putByteAndInt(ValueFormat.INT32, value);
  }

  private void putUInt32(int value) throws IOException {
    putByteAndInt(ValueFormat.UINT32, value);
  }

  private void putInt64(long value) throws IOException {
    putByteAndLong(ValueFormat.INT64, value);
  }

  private void putUInt64(long value) throws IOException {
    putByteAndLong(ValueFormat.UINT64, value);
  }

  private void putFloat32(float value) throws IOException {
    putByteAndFloat(value);
  }

  private void putFloat64(double value) throws IOException {
    putByteAndDouble(value);
  }

  private void writeBuffer() throws IOException {
    buffer.flip();
    sink.write(buffer);
    buffer.clear();
  }

  private void putByteAndShort(byte value1, short value2) throws IOException {
    sink.ensureRemaining(buffer, 3);
    buffer.put(value1);
    buffer.putShort(value2);
  }

  private void putByteAndInt(byte value1, int value2) throws IOException {
    sink.ensureRemaining(buffer, 5);
    buffer.put(value1);
    buffer.putInt(value2);
  }

  private void putByteAndLong(byte value1, long value2) throws IOException {
    sink.ensureRemaining(buffer, 9);
    buffer.put(value1);
    buffer.putLong(value2);
  }

  private void putByteAndFloat(float value) throws IOException {
    sink.ensureRemaining(buffer, 5);
    buffer.put(ValueFormat.FLOAT32);
    buffer.putFloat(value);
  }

  private void putByteAndDouble(double value) throws IOException {
    sink.ensureRemaining(buffer, 9);
    buffer.put(ValueFormat.FLOAT64);
    buffer.putDouble(value);
  }
}
