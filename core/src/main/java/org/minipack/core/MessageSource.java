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
import org.minipack.core.internal.*;

/** The underlying source of a {@link MessageReader}. */
public abstract class MessageSource implements Closeable {
  /** Returns a source that reads from the given input stream. */
  static MessageSource of(InputStream stream) {
    return new InputStreamSource(stream);
  }

  /** Returns a source that reads from the given blocking channel. */
  static MessageSource of(ReadableByteChannel blockingChannel) {
    return new ChannelSource(blockingChannel);
  }

  /**
   * Reads between 1 and {@linkplain ByteBuffer#remaining() remaining} bytes from this source into
   * the given buffer, returning the actual number of bytes read.
   *
   * <p>Returns {@code -1} if no more bytes can be read from this source.
   *
   * <p>{@code minBytesHint} indicates the minimum number of bytes that the caller would like to
   * read. However, unlike {@link #readAtLeast}, this method does not guarantee that more than 1
   * byte will be read.
   */
  public abstract int readAny(ByteBuffer buffer, int minBytesHint) throws IOException;

  /**
   * Reads between {@code minBytes} and {@linkplain ByteBuffer#remaining() remaining} bytes from
   * this source into the given buffer, returning the actual number of bytes read.
   *
   * <p>Throws {@link java.io.EOFException} if the end of input is reached before {@code minBytes}
   * bytes have been read.
   */
  public final int readAtLeast(ByteBuffer buffer, int minBytes) throws IOException {
    assert minBytes <= buffer.remaining();
    var totalBytesRead = 0;
    while (totalBytesRead < minBytes) {
      var bytesRead = readAny(buffer, minBytes);
      if (bytesRead == -1) {
        throw Exceptions.prematureEndOfInput(minBytes, totalBytesRead);
      }
      totalBytesRead += bytesRead;
    }
    return totalBytesRead;
  }

  /**
   * Reads enough bytes from this source into the given buffer for {@linkplain ByteBuffer#get()
   * getting} at least {@code length} bytes from the buffer.
   *
   * <p>The number of bytes read is between 0 and {@link ByteBuffer#remaining()}.
   */
  public final void ensureRemaining(ByteBuffer buffer, int length) throws IOException {
    int minBytes = length - buffer.remaining();
    if (minBytes > 0) {
      buffer.compact();
      readAtLeast(buffer, minBytes);
      buffer.flip();
      assert buffer.remaining() >= length;
    }
  }

  public final byte getByte(ByteBuffer buffer) throws IOException {
    ensureRemaining(buffer, 1);
    return buffer.get();
  }

  public final byte[] getBytes(ByteBuffer buffer, int length) throws IOException {
    ensureRemaining(buffer, length);
    var bytes = new byte[length];
    buffer.get(bytes);
    return bytes;
  }

  public final short getShort(ByteBuffer buffer) throws IOException {
    ensureRemaining(buffer, 2);
    return buffer.getShort();
  }

  public final int getInt(ByteBuffer buffer) throws IOException {
    ensureRemaining(buffer, 4);
    return buffer.getInt();
  }

  public final long getLong(ByteBuffer buffer) throws IOException {
    ensureRemaining(buffer, 8);
    return buffer.getLong();
  }

  /**
   * Gets a MessagePack string header from the given buffer, ensuring that the buffer has enough
   * space remaining.
   */
  public final int getStringHeader(ByteBuffer buffer) throws IOException {
    var format = getByte(buffer);
    return switch (format) {
      case ValueFormat.STR8 -> getLength8(buffer);
      case ValueFormat.STR16 -> getLength16(buffer);
      case ValueFormat.STR32 -> getLength32(buffer, ValueType.STRING);
      default -> {
        if (ValueFormat.isFixStr(format)) {
          yield ValueFormat.getFixStrLength(format);
        }
        throw Exceptions.typeMismatch(format, RequestedType.STRING);
      }
    };
  }

  /**
   * Gets a MessagePack binary header from the given buffer, ensuring that the buffer has enough
   * space remaining.
   */
  public final int getBinaryHeader(ByteBuffer buffer) throws IOException {
    var format = getByte(buffer);
    return switch (format) {
      case ValueFormat.BIN8 -> getLength8(buffer);
      case ValueFormat.BIN16 -> getLength16(buffer);
      case ValueFormat.BIN32 -> getLength32(buffer, ValueType.BINARY);
      default -> throw Exceptions.typeMismatch(format, RequestedType.BINARY);
    };
  }

  /**
   * Gets a MessagePack extension header from the given buffer, ensuring that the buffer has enough
   * space remaining.
   */
  public final ExtensionHeader getExtensionHeader(ByteBuffer buffer) throws IOException {
    var format = getByte(buffer);
    return switch (format) {
      case ValueFormat.FIXEXT1 -> new ExtensionHeader(1, getByte(buffer));
      case ValueFormat.FIXEXT2 -> new ExtensionHeader(2, getByte(buffer));
      case ValueFormat.FIXEXT4 -> new ExtensionHeader(4, getByte(buffer));
      case ValueFormat.FIXEXT8 -> new ExtensionHeader(8, getByte(buffer));
      case ValueFormat.FIXEXT16 -> new ExtensionHeader(16, getByte(buffer));
      case ValueFormat.EXT8 -> new ExtensionHeader(getLength8(buffer), getByte(buffer));
      case ValueFormat.EXT16 -> new ExtensionHeader(getLength16(buffer), getByte(buffer));
      case ValueFormat.EXT32 ->
          new ExtensionHeader(getLength32(buffer, ValueType.EXTENSION), getByte(buffer));
      default -> throw Exceptions.typeMismatch(format, RequestedType.EXTENSION);
    };
  }

  final byte nextByte(ByteBuffer buffer) throws IOException {
    ensureRemaining(buffer, 1);
    return buffer.get(buffer.position());
  }

  final short getUByte(ByteBuffer buffer) throws IOException {
    ensureRemaining(buffer, 1);
    return (short) (buffer.get() & 0xff);
  }

  final int getUShort(ByteBuffer buffer) throws IOException {
    ensureRemaining(buffer, 2);
    return buffer.getShort() & 0xffff;
  }

  final long getUInt(ByteBuffer buffer) throws IOException {
    ensureRemaining(buffer, 4);
    return buffer.getInt() & 0xffffffffL;
  }

  final short getLength8(ByteBuffer buffer) throws IOException {
    return getUByte(buffer);
  }

  final int getLength16(ByteBuffer buffer) throws IOException {
    return getUShort(buffer);
  }

  final int getLength32(ByteBuffer buffer, ValueType type) throws IOException {
    var length = getInt(buffer);
    if (length < 0) {
      throw Exceptions.lengthOverflow(length & 0xffffffffL, type);
    }
    return length;
  }
}
