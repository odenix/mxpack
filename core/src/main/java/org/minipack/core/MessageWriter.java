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
  private final Encoder<CharSequence> stringEncoder;
  private final Encoder<String> identifierEncoder;

  /** A builder of {@code MessageWriter}. */
  public static final class Builder {
    private Builder() {}

    private @Nullable MessageSink sink;
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
    stringEncoder = builder.stringEncoder;
    identifierEncoder = builder.identifierEncoder;
  }

  /** Writes a nil (null) value. */
  public void writeNil() throws IOException {
    sink.putByte(ValueFormat.NIL);
  }

  /** Writes a boolean value. */
  public void write(boolean value) throws IOException {
    sink.putByte(value ? ValueFormat.TRUE : ValueFormat.FALSE);
  }

  /** Writes an integer value that fits into a Java byte. */
  public void write(byte value) throws IOException {
    if (value < -(1 << 5)) {
      writeInt8(value);
    } else {
      sink.putByte(value);
    }
  }

  /** Writes an integer value that fits into a Java short. */
  public void write(short value) throws IOException {
    if (value < -(1 << 5)) {
      if (value < -(1 << 7)) {
        writeInt16(value);
      } else {
        writeInt8((byte) value);
      }
    } else if (value < (1 << 7)) {
      sink.putByte((byte) value);
    } else {
      if (value < (1 << 8)) {
        writeUInt8((byte) value);
      } else {
        writeUInt16(value);
      }
    }
  }

  /** Writes an integer value that fits into a Java int. */
  public void write(int value) throws IOException {
    if (value < -(1 << 5)) {
      if (value < -(1 << 15)) {
        writeInt32(value);
      } else {
        if (value < -(1 << 7)) {
          writeInt16((short) value);
        } else {
          writeInt8((byte) value);
        }
      }
    } else if (value < (1 << 7)) {
      sink.putByte((byte) value);
    } else {
      if (value < (1 << 16)) {
        if (value < (1 << 8)) {
          writeUInt8((byte) value);
        } else {
          writeUInt16((short) value);
        }
      } else {
        writeUInt32(value);
      }
    }
  }

  /** Writes an integer value that fits into a Java long. */
  public void write(long value) throws IOException {
    if (value < -(1L << 5)) {
      if (value < -(1L << 31)) {
        writeInt64(value);
      } else {
        if (value < -(1L << 15)) {
          writeInt32((int) value);
        } else {
          if (value < -(1L << 7)) {
            writeInt16((short) value);
          } else {
            writeInt8((byte) value);
          }
        }
      }
    } else if (value < (1L << 7)) {
      sink.putByte((byte) value);
    } else {
      if (value < (1L << 32)) {
        if (value < (1L << 16)) {
          if (value < (1L << 8)) {
            writeUInt8((byte) value);
          } else {
            writeUInt16((short) value);
          }
        } else {
          writeUInt32((int) value);
        }
      } else {
        writeUInt64(value);
      }
    }
  }

  /** Writes a floating point value that fits into a Java float. */
  public void write(float value) throws IOException {
    writeFloat32(value);
  }

  /** Writes a floating point value that fits into a Java double. */
  public void write(double value) throws IOException {
    writeFloat64(value);
  }

  /** Writes a timestamp value. */
  public void write(Instant value) throws IOException {
    var seconds = value.getEpochSecond();
    var nanos = value.getNano();
    if (nanos == 0 && seconds >= 0 && seconds < (1L << 32)) {
      writeExtensionHeader(4, TIMESTAMP_EXTENSION_TYPE);
      sink.putInt((int) seconds);
    } else if (seconds >= 0 && seconds < (1L << 34)) {
      writeExtensionHeader(8, TIMESTAMP_EXTENSION_TYPE);
      sink.putLong(((long) nanos) << 34 | seconds);
    } else {
      writeExtensionHeader(12, TIMESTAMP_EXTENSION_TYPE);
      sink.putInt(nanos);
      sink.putLong(seconds);
    }
  }

  /** Writes a string value. */
  public void write(CharSequence string) throws IOException {
    stringEncoder.encode(string, sink, this);
  }

  /** Writes an identifier value. */
  public void writeIdentifier(String identifier) throws IOException {
    identifierEncoder.encode(identifier, sink, this);
  }

  public <T> void write(T value, Encoder<T> encoder) throws IOException {
    encoder.encode(value, sink, this);
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
      sink.putByte((byte) (ValueFormat.FIXARRAY_PREFIX | elementCount));
    } else if (elementCount < (1 << 16)) {
      sink.putByteAndShort(ValueFormat.ARRAY16, (short) elementCount);
    } else {
      sink.putByteAndInt(ValueFormat.ARRAY32, elementCount);
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
      sink.putByte((byte) (ValueFormat.FIXMAP_PREFIX | entryCount));
    } else if (entryCount < (1 << 16)) {
      sink.putByteAndShort(ValueFormat.MAP16, (short) entryCount);
    } else {
      sink.putByteAndInt(ValueFormat.MAP32, entryCount);
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
      sink.putByte((byte) (ValueFormat.FIXSTR_PREFIX | length));
    } else if (length < (1 << 8)) {
      sink.putBytes(ValueFormat.STR8, (byte) length);
    } else if (length < (1 << 16)) {
      sink.putByteAndShort(ValueFormat.STR16, (short) length);
    } else {
      sink.putByteAndInt(ValueFormat.STR32, length);
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
      sink.putBytes(ValueFormat.BIN8, (byte) length);
    } else if (length < (1 << 16)) {
      sink.putByteAndShort(ValueFormat.BIN16, (short) length);
    } else {
      sink.putByteAndInt(ValueFormat.BIN32, length);
    }
  }

  public void writeExtensionHeader(int length, byte type) throws IOException {
    if (length < 0) {
      throw Exceptions.negativeLength(length);
    }
    switch (length) {
      case 1 -> sink.putBytes(ValueFormat.FIXEXT1, type);
      case 2 -> sink.putBytes(ValueFormat.FIXEXT2, type);
      case 4 -> sink.putBytes(ValueFormat.FIXEXT4, type);
      case 8 -> sink.putBytes(ValueFormat.FIXEXT8, type);
      case 16 -> sink.putBytes(ValueFormat.FIXEXT16, type);
      default -> {
        if (length < (1 << 8)) {
          sink.putBytes(ValueFormat.EXT8, (byte) length);
        } else if (length < (1 << 16)) {
          sink.putByteAndShort(ValueFormat.EXT16, (short) length);
        } else {
          sink.putByteAndInt(ValueFormat.EXT32, length);
        }
        sink.putByte(type);
      }
    }
  }

  public void writePayload(ByteBuffer buffer) throws IOException {
    sink.flushBuffer();
    sink.write(buffer);
  }

  private void writeInt8(byte value) throws IOException {
    sink.putBytes(ValueFormat.INT8, value);
  }

  private void writeUInt8(byte value) throws IOException {
    sink.putBytes(ValueFormat.UINT8, value);
  }

  private void writeInt16(short value) throws IOException {
    sink.putByteAndShort(ValueFormat.INT16, value);
  }

  private void writeUInt16(short value) throws IOException {
    sink.putByteAndShort(ValueFormat.UINT16, value);
  }

  private void writeInt32(int value) throws IOException {
    sink.putByteAndInt(ValueFormat.INT32, value);
  }

  private void writeUInt32(int value) throws IOException {
    sink.putByteAndInt(ValueFormat.UINT32, value);
  }

  private void writeInt64(long value) throws IOException {
    sink.putByteAndLong(ValueFormat.INT64, value);
  }

  private void writeUInt64(long value) throws IOException {
    sink.putByteAndLong(ValueFormat.UINT64, value);
  }

  private void writeFloat32(float value) throws IOException {
    sink.putByteAndFloat(ValueFormat.FLOAT32, value);
  }

  private void writeFloat64(double value) throws IOException {
    sink.putByteAndDouble(ValueFormat.FLOAT64, value);
  }

  /**
   * Writes any data remaining in this writer's buffer and flushes the underlying message
   * {@linkplain MessageSink sink}.
   */
  public void flush() throws IOException {
    sink.flushBuffer();
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
}
