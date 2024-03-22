/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.jspecify.annotations.Nullable;

/**
 * Writes messages encoded in the <a href="https://msgpack.org/">MessagePack</a> binary
 * serialization format.
 *
 * <p>To create a {@code MessageWriter}, use a {@linkplain #builder() builder}. To write a message,
 * call one of the {@code write()} or {@code writeXYZ()} methods. To flush the underlying message
 * {@linkplain MessageSink sink}, call {@link #flush()}. If an error occurs when writing a message,
 * a {@link WriterException} is thrown.
 */
public final class MessageWriter {
  private static final int MIN_BUFFER_SIZE = 9;
  private static final int DEFAULT_BUFFER_SIZE = 1 << 13;

  private final MessageSink sink;
  private final ByteBuffer buffer;

  public static final class Builder {
    private Builder() {}

    private @Nullable MessageSink sink;
    private @Nullable ByteBuffer buffer;

    public Builder sink(OutputStream stream) {
      sink = new MessageSinks.OutputStreamSink(stream);
      return this;
    }

    public Builder sink(WritableByteChannel channel) {
      sink = new MessageSinks.ChannelSink(channel);
      return this;
    }

    public Builder sink(MessageSink sink) {
      this.sink = sink;
      return this;
    }

    public Builder buffer(ByteBuffer buffer) {
      this.buffer = buffer;
      return this;
    }

    public MessageWriter build() {
      return new MessageWriter(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private MessageWriter(Builder builder) {
    if (builder.sink == null) {
      throw WriterException.sinkRequired();
    }
    sink = builder.sink;
    buffer =
        builder.buffer != null ? builder.buffer.clear() : ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    if (buffer.capacity() < MIN_BUFFER_SIZE) {
      throw new IllegalArgumentException(
          "MessageWriter requires a buffer with a capacity of at least " + MIN_BUFFER_SIZE + ".");
    }
  }

  public void writeNil() {
    ensureRemaining(1);
    buffer.put(Format.NIL);
  }

  public void write(boolean value) {
    ensureRemaining(1);
    buffer.put(value ? Format.TRUE : Format.FALSE);
  }

  public void write(byte value) {
    if (value < -(1 << 5)) {
      writeInt8(value);
    } else {
      writeFixInt(value);
    }
  }

  public void write(short value) {
    if (value < -(1 << 5)) {
      if (value < -(1 << 7)) {
        writeInt16(value);
      } else {
        writeInt8((byte) value);
      }
    } else if (value < (1 << 7)) {
      writeFixInt((byte) value);
    } else {
      if (value < (1 << 8)) {
        writeUInt8((byte) value);
      } else {
        writeUInt16(value);
      }
    }
  }

  public void write(int value) {
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
      writeFixInt((byte) value);
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

  public void write(long value) {
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
      writeFixInt((byte) value);
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

  public void write(float value) {
    ensureRemaining(5);
    buffer.put(Format.FLOAT32);
    buffer.putFloat(value);
  }

  public void write(double value) {
    ensureRemaining(9);
    buffer.put(Format.FLOAT64);
    buffer.putDouble(value);
  }

  public void write(CharSequence str) {
    var utf8Length = countUtf8Length(str);
    if (utf8Length < 0) {
      writeStringHeader(-utf8Length);
      writeStringAscii(str);
    } else {
      writeStringHeader(utf8Length);
      writeStringNonAscii(str);
    }
  }

  public void writeArrayHeader(int length) {
    if (length < (1 << 4)) {
      ensureRemaining(1);
      buffer.put((byte) (Format.FIXARRAY_PREFIX | length));
    } else if (length < (1 << 16)) {
      ensureRemaining(3);
      buffer.put(Format.ARRAY16);
      buffer.putShort((short) length);
    } else {
      ensureRemaining(5);
      buffer.put(Format.ARRAY32);
      buffer.putInt(length);
    }
  }

  public void writeMapHeader(int size) {
    if (size < (1 << 4)) {
      ensureRemaining(1);
      buffer.put((byte) (Format.FIXMAP_PREFIX | size));
    } else if (size < (1 << 16)) {
      ensureRemaining(3);
      buffer.put(Format.MAP16);
      buffer.putShort((short) size);
    } else {
      ensureRemaining(5);
      buffer.put(Format.MAP32);
      buffer.putInt(size);
    }
  }

  public void writeStringHeader(int utf8Length) {
    if (utf8Length < (1 << 5)) {
      ensureRemaining(1);
      buffer.put((byte) (Format.FIXSTR_PREFIX | utf8Length));
    } else if (utf8Length < (1 << 8)) {
      ensureRemaining(2);
      buffer.put(Format.STR8);
      buffer.put((byte) utf8Length);
    } else if (utf8Length < (1 << 16)) {
      ensureRemaining(3);
      buffer.put(Format.STR16);
      buffer.putShort((short) utf8Length);
    } else {
      ensureRemaining(5);
      buffer.put(Format.STR32);
      buffer.putInt(utf8Length);
    }
  }

  public void writeBinaryHeader(int length) {
    if (length < (1 << 8)) {
      ensureRemaining(2);
      buffer.put(Format.BIN8);
      buffer.put((byte) length);
    } else if (length < (1 << 16)) {
      ensureRemaining(3);
      buffer.put(Format.BIN16);
      buffer.putShort((short) length);
    } else {
      ensureRemaining(5);
      buffer.put(Format.BIN32);
      buffer.putInt(length);
    }
  }

  public void writeBinaryPayload(ByteBuffer buffer) {
    writeBuffer();
    doWriteBuffer(buffer);
  }

  public void flush() {
    writeBuffer();
    try {
      sink.flush();
    } catch (IOException e) {
      throw WriterException.ioErrorFlushingSink(e);
    }
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
      writeBuffer();
    }
  }

  private void writeStringNonAscii(CharSequence str) {
    var length = str.length();
    for (var i = 0; i < length; i++) {
      var ch = str.charAt(i);
      ensureRemaining(4);
      if (ch < 0x80) { // 1 byte
        buffer.put((byte) ch);
      } else if (ch < 0x800) { // 2 bytes
        buffer.put((byte) (0xc0 | ch >>> 6));
        buffer.put((byte) (0x80 | (ch & 0x3f)));
      } else if (Character.isSurrogate(ch)) { // 4 bytes
        char ch2;
        if (++i == length || !Character.isSurrogatePair(ch, ch2 = str.charAt(i))) {
          throw WriterException.invalidSurrogatePair(i);
        }
        var cp = Character.toCodePoint(ch, ch2);
        buffer.put((byte) (0xf0 | cp >>> 18));
        buffer.put((byte) (0x80 | ((cp >>> 12) & 0x3f)));
        buffer.put((byte) (0x80 | ((cp >>> 6) & 0x3f)));
        buffer.put((byte) (0x80 | (cp & 0x3f)));
      } else { // 3 bytes
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

  private void writeBuffer() {
    buffer.flip();
    doWriteBuffer(buffer);
    buffer.clear();
  }

  private void doWriteBuffer(ByteBuffer buffer) {
    try {
      sink.writeBuffer(buffer);
    } catch (IOException e) {
      throw WriterException.ioErrorWritingToSink(e);
    }
  }

  private void writeFixInt(byte value) {
    ensureRemaining(1);
    buffer.put(value);
  }

  private void writeInt8(byte value) {
    ensureRemaining(2);
    buffer.put(Format.INT8);
    buffer.put(value);
  }

  private void writeUInt8(byte value) {
    ensureRemaining(2);
    buffer.put(Format.UINT8);
    buffer.put(value);
  }

  private void writeInt16(short value) {
    ensureRemaining(3);
    buffer.put(Format.INT16);
    buffer.putShort(value);
  }

  private void writeUInt16(short value) {
    ensureRemaining(3);
    buffer.put(Format.UINT16);
    buffer.putShort(value);
  }

  private void writeInt32(int value) {
    ensureRemaining(5);
    buffer.put(Format.INT32);
    buffer.putInt(value);
  }

  private void writeUInt32(int value) {
    ensureRemaining(5);
    buffer.put(Format.UINT32);
    buffer.putInt(value);
  }

  private void writeInt64(long value) {
    ensureRemaining(9);
    buffer.put(Format.INT64);
    buffer.putLong(value);
  }

  private void writeUInt64(long value) {
    ensureRemaining(9);
    buffer.put(Format.UINT64);
    buffer.putLong(value);
  }

  private void ensureRemaining(int length) {
    if (buffer.remaining() < length) writeBuffer();
  }
}
