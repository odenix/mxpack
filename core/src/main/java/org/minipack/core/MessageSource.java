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
  private static final int MIN_BUFFER_SIZE = 9;

  private final ByteBuffer buffer;

  /** Returns a source that reads from the given input stream. */
  static MessageSource of(InputStream stream) {
    return new InputStreamSource(stream);
  }

  /** Returns a source that reads from the given input stream. */
  static MessageSource of(InputStream stream, ByteBuffer buffer) {
    return new InputStreamSource(stream, buffer);
  }

  /** Returns a source that reads from the given blocking channel. */
  static MessageSource of(ReadableByteChannel blockingChannel) {
    return new ChannelSource(blockingChannel);
  }

  /** Returns a source that reads from the given blocking channel. */
  static MessageSource of(ReadableByteChannel blockingChannel, ByteBuffer buffer) {
    return new ChannelSource(blockingChannel, buffer);
  }

  public MessageSource(ByteBuffer buffer) {
    if (buffer.capacity() < MIN_BUFFER_SIZE) {
      throw Exceptions.bufferTooSmall(buffer.capacity(), MIN_BUFFER_SIZE);
    }
    this.buffer = buffer.position(0).limit(0);
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

  public ByteBuffer buffer() {
    return buffer;
  }

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
  public final void ensureRemaining(int length) throws IOException {
    int minBytes = length - buffer.remaining();
    if (minBytes > 0) {
      buffer.compact();
      readAtLeast(buffer, minBytes);
      buffer.flip();
    }
  }

  public final byte nextByte() throws IOException {
    ensureRemaining(1);
    return buffer.get(buffer.position());
  }

  public final byte getByte() throws IOException {
    ensureRemaining(1);
    return buffer.get();
  }

  public final byte[] getBytes(int length) throws IOException {
    ensureRemaining(length);
    var bytes = new byte[length];
    buffer.get(bytes);
    return bytes;
  }

  public final short getShort() throws IOException {
    ensureRemaining(2);
    return buffer.getShort();
  }

  public final int getInt() throws IOException {
    ensureRemaining(4);
    return buffer.getInt();
  }

  public final long getLong() throws IOException {
    ensureRemaining(8);
    return buffer.getLong();
  }

  public final float getFloat() throws IOException {
    ensureRemaining(4);
    return buffer.getFloat();
  }

  public final double getDouble() throws IOException {
    ensureRemaining(8);
    return buffer.getDouble();
  }

  public short getUByte() throws IOException {
    return (short) (getByte() & 0xff);
  }

  public int getUShort() throws IOException {
    return getShort() & 0xffff;
  }

  public long getUInt() throws IOException {
    return getInt() & 0xffffffffL;
  }

  public short getLength8() throws IOException {
    return getUByte();
  }

  public int getLength16() throws IOException {
    return getUShort();
  }

  public int getLength32(ValueType type) throws IOException {
    var length = getInt();
    if (length < 0) {
      throw Exceptions.lengthOverflow(length & 0xffffffffL, type);
    }
    return length;
  }
}
