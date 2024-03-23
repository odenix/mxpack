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
import org.translatenix.minipack.internal.ValueFormat;

/**
 * Writes messages encoded in the <a href="https://msgpack.org/">MessagePack</a> binary
 * serialization format.
 *
 * <p>To create a new {@code MessageWriter}, use a {@linkplain #builder() builder}. To write a
 * value, call one of the {@code write()} or {@code writeXYZ()} methods. To flush the underlying
 * message {@linkplain MessageSink sink}, call {@link #flush()}.
 *
 * <p>Unless otherwise noted, methods throw {@link WriterException} if an error occurs. The most
 * common type of error is an I/O error.
 */
public final class MessageWriter implements Closeable {
  private static final int MIN_BUFFER_CAPACITY = 9;
  private static final int DEFAULT_BUFFER_CAPACITY = 1 << 13;

  private final MessageSink sink;
  private final ByteBuffer buffer;

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

    /** Shorthand for {@code sink(MessageSink.of(stream))}. */
    public Builder sink(OutputStream stream) {
      return sink(MessageSink.of(stream));
    }

    /** Shorthand for {@code sink(MessageSink.of(channel))}. */
    public Builder sink(WritableByteChannel channel) {
      return sink(MessageSink.of(channel));
    }

    /**
     * Sets the buffer to use for writing to the underlying message {@linkplain MessageSink sink}.
     */
    public Builder buffer(ByteBuffer buffer) {
      this.buffer = buffer;
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
        builder.buffer != null
            ? builder.buffer.clear()
            : ByteBuffer.allocate(DEFAULT_BUFFER_CAPACITY);
    if (buffer.capacity() < MIN_BUFFER_CAPACITY) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_CAPACITY);
    }
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
  public void write(CharSequence string) {
    var length = countUtf8Length(string);
    if (length < 0) {
      writeRawStringHeader(-length);
      writeStringAscii(string);
    } else {
      writeRawStringHeader(length);
      writeStringNonAscii(string);
    }
  }

  /**
   * Starts writing an array value with the given number of elements.
   *
   * <p>A call to this method MUST be followed by {@code elementCount} calls that write the array's
   * elements.
   */
  public void writeArrayHeader(int elementCount) {
    requireValidLength(elementCount);
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
    requireValidLength(entryCount);
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
   * <p>This method is a low-level alternative to {@link #write(CharSequence)}. It can be useful in
   * the following cases:
   *
   * <ul>
   *   <li>The string to write is already available as UTF-8 byte sequence.
   *   <li>Full control over conversion from {@code java.lang.String} to UTF-8 is required.
   * </ul>
   */
  public void writeRawStringHeader(int byteCount) {
    requireValidLength(byteCount);
    if (byteCount < (1 << 5)) {
      putByte((byte) (ValueFormat.FIXSTR_PREFIX | byteCount));
    } else if (byteCount < (1 << 8)) {
      putByte(ValueFormat.STR8, (byte) byteCount);
    } else if (byteCount < (1 << 16)) {
      putShort(ValueFormat.STR16, (short) byteCount);
    } else {
      putInt(ValueFormat.STR32, byteCount);
    }
  }

  /**
   * Starts writing a binary value with the given number of bytes.
   *
   * <p>A call to this method MUST be followed by one or more calls to {@link #writePayload} that
   * write exactly {@code byteCount} bytes in total.
   */
  public void writeBinaryHeader(int byteCount) {
    requireValidLength(byteCount);
    if (byteCount < (1 << 8)) {
      putByte(ValueFormat.BIN8, (byte) byteCount);
    } else if (byteCount < (1 << 16)) {
      putShort(ValueFormat.BIN16, (short) byteCount);
    } else {
      putInt(ValueFormat.BIN32, byteCount);
    }
  }

  public void writeExtensionHeader(int byteCount, byte type) {
    requireValidLength(byteCount);
    if (type < 0) throw Exceptions.invalidExtensionType(type);
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
   * <p>This method is used together with {@link #writeBinaryHeader} or {@link
   * #writeRawStringHeader}.
   */
  public int writePayload(ByteBuffer buffer) {
    writeEntireBuffer();
    return writeToSink(buffer);
  }

  public int writePayload(ByteBuffer buffer, int minBytesToWrite) {
    writeEntireBuffer();
    return writeToSink(buffer, minBytesToWrite);
  }

  /**
   * Writes any data remaining in this writer's buffer and flushes the underlying message
   * {@linkplain MessageSink sink}.
   */
  public void flush() {
    writeEntireBuffer();
    try {
      sink.flush();
    } catch (IOException e) {
      throw Exceptions.ioErrorFlushingSink(e);
    }
  }

  /**
   * {@linkplain #flush() Flushes} this writer and {@linkplain MessageSink#close() closes} the
   * underlying message {@linkplain MessageSink sink}.
   */
  @Override
  public void close() {
    flush();
    try {
      sink.close();
    } catch (IOException e) {
      throw Exceptions.ioErrorClosingSink(e);
    }
  }

  private void requireValidLength(int length) {
    if (length < 0) throw Exceptions.invalidLength(length);
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

  private void writeStringAscii(CharSequence str) {
    var length = str.length();
    var i = 0;
    while (true) { // repeat filling and writing buffer until done
      var nextStop = Math.min(length, i + buffer.remaining());
      for (; i < nextStop; i++) {
        buffer.put((byte) str.charAt(i));
      }
      if (i == length) break;
      buffer.flip();
      writeToSink(buffer);
      buffer.compact();
    }
  }

  private void writeStringNonAscii(CharSequence str) {
    var length = str.length();
    for (var i = 0; i < length; i++) {
      var ch = str.charAt(i);
      if (ch < 0x80) {
        ensureRemaining(1);
        buffer.put((byte) ch);
      } else if (ch < 0x800) {
        ensureRemaining(2);
        buffer.put((byte) (0xc0 | ch >>> 6));
        buffer.put((byte) (0x80 | (ch & 0x3f)));
      } else if (Character.isSurrogate(ch)) {
        char ch2;
        if (++i == length || !Character.isSurrogatePair(ch, ch2 = str.charAt(i))) {
          throw Exceptions.invalidSurrogatePair(i);
        }
        var cp = Character.toCodePoint(ch, ch2);
        ensureRemaining(4);
        buffer.put((byte) (0xf0 | cp >>> 18));
        buffer.put((byte) (0x80 | ((cp >>> 12) & 0x3f)));
        buffer.put((byte) (0x80 | ((cp >>> 6) & 0x3f)));
        buffer.put((byte) (0x80 | (cp & 0x3f)));
      } else {
        ensureRemaining(3);
        buffer.put((byte) (0xe0 | ch >>> 12));
        buffer.put((byte) (0x80 | ((ch >>> 6) & 0x3f)));
        buffer.put((byte) (0x80 | (ch & 0x3f)));
      }
    }
  }

  private int countUtf8Length(CharSequence str) {
    var length = str.length();
    var i = 0;
    for (; i < length; i++) {
      if (str.charAt(i) >= 0x80) break;
    }
    if (i == length) return -length;

    var result = i;
    for (; i < length; i++) {
      var ch = str.charAt(i);
      if (ch < 0x80) {
        result += 1;
      } else if (ch < 0x800) {
        result += 2;
      } else if (Character.isSurrogate(ch)) {
        // leave validation to writeStringNonAscii
        result += 4;
        i += 1;
      } else {
        result += 3;
      }
    }
    return result;
  }

  private void ensureRemaining(int byteCount) {
    assert byteCount <= buffer.capacity();
    var minBytesToWrite = byteCount - buffer.remaining();
    if (minBytesToWrite <= 0) return;
    buffer.flip();
    writeToSink(buffer, minBytesToWrite);
    buffer.compact();
  }

  private void writeEntireBuffer() {
    buffer.flip();
    while (buffer.hasRemaining()) {
      writeToSink(buffer);
    }
    buffer.clear();
  }

  private int writeToSink(ByteBuffer buffer, int minBytes) {
    var totalBytesWritten = 0;
    while (totalBytesWritten < minBytes) {
      var bytesWritten = writeToSink(buffer);
      totalBytesWritten += bytesWritten;
    }
    return totalBytesWritten;
  }

  private int writeToSink(ByteBuffer buffer) {
    try {
      return sink.write(buffer);
    } catch (IOException e) {
      throw Exceptions.ioErrorWritingToSink(e);
    }
  }
}
