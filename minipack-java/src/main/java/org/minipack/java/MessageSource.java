/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;
import org.minipack.java.internal.*;

/// The underlying source of a [DefaultMessageReader].
public interface MessageSource extends Closeable {
  static MessageSource of(ReadableByteChannel channel) {
    return new DefaultMessageSource(new ChannelSourceProvider(channel));
  }

  static MessageSource of(ReadableByteChannel channel, Consumer<Options> consumer) {
    return new DefaultMessageSource(new ChannelSourceProvider(channel), consumer);
  }

  static MessageSource of(InputStream stream) {
    return new DefaultMessageSource(new StreamSourceProvider(stream));
  }

  static MessageSource of(InputStream stream, Consumer<Options> consumer) {
    return new DefaultMessageSource(new StreamSourceProvider(stream), consumer);
  }

  static MessageSource of(BufferAllocator.PooledByteBuffer buffer) {
    return new DefaultMessageSource(new EmptySourceProvider(), options -> {}, buffer);
  }

  static MessageSource of(BufferAllocator.PooledByteBuffer buffer, Consumer<Options> consumer) {
    return new DefaultMessageSource(new EmptySourceProvider(), consumer, buffer);
  }

  static MessageSource of(ByteBuffer buffer) {
    return new DefaultMessageSource(new EmptySourceProvider(), options -> {}, buffer);
  }

  static MessageSource of(ByteBuffer buffer, Consumer<Options> consumer) {
    return new DefaultMessageSource(new EmptySourceProvider(), consumer, buffer);
  }

  static MessageSource of(Provider provider) {
    return new DefaultMessageSource(provider);
  }

  static MessageSource of(Provider provider, Consumer<Options> consumer) {
    return new DefaultMessageSource(provider, consumer);
  }

  interface Options {
    Options allocator(BufferAllocator allocator);

    @SuppressWarnings("UnusedReturnValue")
    Options bufferCapacity(int capacity);
  }

  interface Provider {
    int read(ByteBuffer buffer, int minBytesHint) throws IOException;

    void close() throws IOException;

    default void skip(int length, ByteBuffer buffer) throws IOException {
      if (length == 0) return;
      var remaining = buffer.remaining();
      if (length <= remaining) {
        buffer.position(buffer.position() + length);
        return;
      }
      var capacity = buffer.capacity();
      var bytesLeft = length - remaining;
      while (bytesLeft > 0) {
        var bytesToRead = Math.min(bytesLeft, capacity);
        buffer.position(0).limit(bytesToRead);
        var bytesRead = read(buffer, bytesToRead);
        if (bytesRead == -1) {
          throw Exceptions.unexpectedEndOfInput(bytesLeft);
        }
        bytesLeft -= bytesRead;
      }
    }

    default long transferTo(WritableByteChannel destination, long length, ByteBuffer buffer)
        throws IOException {
      if (length == 0) return 0;
      var bytesLeft = length;
      for (var remaining = buffer.remaining(); bytesLeft > buffer.remaining(); ) {
        var bytesWritten = destination.write(buffer);
        if (bytesWritten != remaining) {
          throw Exceptions.nonBlockingChannelDetected();
        }
        bytesLeft -= bytesWritten;
        assert bytesLeft > 0;
        buffer.clear();
        var bytesRead = read(buffer, 1);
        buffer.flip();
        if (bytesRead == -1) {
          return length - bytesLeft;
        }
      }
      assert bytesLeft <= buffer.remaining();
      var savedLimit = buffer.limit();
      buffer.limit(buffer.position() + (int) bytesLeft);
      var bytesWritten = destination.write(buffer);
      if (bytesWritten != bytesLeft) {
        throw Exceptions.nonBlockingChannelDetected();
      }
      buffer.limit(savedLimit);
      return length;
    }
  }

  ByteBuffer buffer();

  BufferAllocator allocator();

  /// Reads between 1 and {@linkplain ByteBuffer#remaining() remaining} bytes from this source into
  /// the given buffer, returning the actual number of bytes read.
  ///
  /// Returns `-1` if no more bytes can be read from this source.
  ///
  /// `minBytesHint` indicates the minimum number of bytes that the caller would like to
  /// read. However, unlike [#readAtLeast], this method does not guarantee that more than 1
  /// byte will be read.
  int read(ByteBuffer buffer) throws IOException;

  /// Reads between `minBytes` and {@linkplain ByteBuffer#remaining() remaining} bytes from
  /// this source into the given buffer, returning the actual number of bytes read.
  ///
  /// Throws [java.io.EOFException] if the end of input is reached before `minBytes`
  /// bytes have been read.
  int readAtLeast(ByteBuffer buffer, int minBytes) throws IOException;

  long transferTo(WritableByteChannel destination, long maxBytesToTransfer) throws IOException;

  /// Reads enough bytes from this source into the given buffer for {@linkplain ByteBuffer#get()
  ///  getting} at least `length` bytes from the buffer.
  ///
  /// The number of bytes read is between 0 and [#remaining()].
  void ensureRemaining(int length) throws IOException;

  void skip(int length) throws IOException;

  byte nextByte() throws IOException;

  byte readByte() throws IOException;

  byte[] readBytes(int length) throws IOException;

  short readShort() throws IOException;

  int readInt() throws IOException;

  long readLong() throws IOException;

  float readFloat() throws IOException;

  double readDouble() throws IOException;

  short readUByte() throws IOException;

  int readUShort() throws IOException;

  long readUInt() throws IOException;

  short readLength8() throws IOException;

  int readLength16() throws IOException;

  int readLength32(MessageType type) throws IOException;

  @Override
  void close() throws IOException;
}
