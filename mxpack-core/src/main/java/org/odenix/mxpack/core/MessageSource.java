/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.odenix.mxpack.core.internal.Exceptions;
import org.odenix.mxpack.core.internal.MessageSourceImpl;

/// A source of bytes that a [MessageReader] reads from.
public sealed interface MessageSource permits MessageSourceImpl {
  /// Adapts a [MessageSource] to an underlying I/O abstraction such as [ReadableByteChannel] or [InputStream].
  interface Provider {
    /// Reads from the underlying I/O abstraction into the given buffer.
    ///
    /// @param buffer the buffer to read into
    /// @param minLengthHint A hint as to how many bytes the caller will try to read immediately.
    ///        If the underlying I/O abstraction has at least `minLengthHint` bytes readily available,
    ///        it should return them without making a blocking I/O call.
    /// @return the actual number of bytes read, or `-1` if the end of input has been reached
    /// @throws IOException if an I/O error occurs
    int read(ByteBuffer buffer, int minLengthHint) throws IOException;

    /// Closes the underlying I/O abstraction.
    ///
    /// This method is called at most once.
    /// No other method is called after this method.
    ///
    /// @throws IOException if an I/O error occurs
    void close() throws IOException;

    /// Transfers up to `length` bytes from the underlying I/O abstraction to the given channel.
    ///
    /// Fewer than `length` bytes may be transferred if this message source reaches the end of input.
    ///
    /// @param channel the channel to transfer bytes to
    /// @param length the number of bytes to transfer
    /// @param buffer A buffer whose `n` remaining bytes must be transferred before transferring
    ///               up to `length - n` bytes from the underlying I/O abstraction.
    ///               The buffer can also be used for transferring bytes from the underlying I/O abstraction.
    /// @return the actual number of bytes transferred (between 0 and `length`)
    /// @throws IOException if an I/O error occurs
    default long transferTo(WritableByteChannel channel, long length, ByteBuffer buffer)
        throws IOException {
      if (length == 0) return 0;
      var bytesLeft = length;
      for (var remaining = buffer.remaining(); bytesLeft > remaining; remaining = buffer.remaining()) {
        var bytesWritten = channel.write(buffer);
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
      var bytesWritten = channel.write(buffer);
      if (bytesWritten != bytesLeft) {
        throw Exceptions.nonBlockingChannelDetected();
      }
      buffer.limit(savedLimit);
      return length;
    }

    /// Skips the next `length` bytes in the underlying I/O abstraction.
    ///
    /// @param length the number of bytes to skip
    /// @param buffer a buffer whose `n` remaining bytes must be skipped before
    ///        skipping `length - n` bytes in the underlying I/O abstraction.
    ///        The buffer can also be used for skipping bytes in the underlying I/O abstraction.
    /// @throws EOFException if this message source reaches the end of input before `length` bytes have been skipped
    /// @throws IOException if an I/O error occurs
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
  }

  /// Returns the read buffer of this message source.
  ///
  /// @return the read buffer of this message source
  ByteBuffer buffer();

  /// Returns the buffer allocator used by this message source.
  ///
  /// @return the buffer allocator used by this message source
  BufferAllocator allocator();

  /// Reads between 1 and [remaining][ByteBuffer#remaining()] bytes into the given byte buffer.
  ///
  /// @param buffer the byte buffer to read into
  /// @return the actual number of bytes read, or `-1` if the end of input has been reached
  /// @throws IOException if an I/O error occurs
  int read(ByteBuffer buffer) throws IOException;

  /// Reads between `length` and [remaining][ByteBuffer#remaining()] bytes into the given byte buffer.
  ///
  /// @param buffer the byte buffer to read into
  /// @param length the minimum number of bytes to read
  /// @return the actual number of bytes read (between `length` and [ByteBuffer#remaining()])
  /// @throws EOFException if the end of input is reached before `length` bytes have been read
  /// @throws IOException if an I/O error occurs
  @SuppressWarnings("UnusedReturnValue")
  int readAtLeast(ByteBuffer buffer, int length) throws IOException;

  /// Transfers up to `length` bytes from this message source to the given channel.
  ///
  /// Fewer than `length` bytes may be transferred if this message source reaches the end of input.
  ///
  /// @param channel the channel to write to
  /// @param length the number of bytes to transfer
  /// @return the actual number of bytes transferred (between 0 and `length`)
  /// @throws IOException if an I/O error occurs
  long transferTo(WritableByteChannel channel, long length) throws IOException;

  /// Reads enough bytes from this message source for [#buffer()] to have
  /// at least `length` bytes [remaining][ByteBuffer#remaining()].
  ///
  /// The number of bytes read is between 0 and [ByteBuffer#capacity()].
  ///
  /// @param length the minimum number of bytes that [#buffer()] needs to have remaining
  /// @throws EOFException if the end of input is reached before [#buffer()] has `length` bytes remaining
  /// @throws IOException if an I/O error occurs
  void ensureRemaining(int length) throws IOException;

  /// Skips the next `length` bytes.
  ///
  /// @param length the number of bytes to skip
  /// @throws EOFException if the end of input is reached before `length` bytes have been skipped
  /// @throws IOException if an I/O error occurs
  void skip(int length) throws IOException;

  /// Reads the next `byte` without consuming it.
  ///
  /// Consecutive calls to this method will return the same result.
  ///
  /// @return the `byte` read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  byte nextByte() throws IOException;

  /// Reads a `byte`.
  ///
  /// @return the `byte` read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  byte readByte() throws IOException;

  /// Reads a `short`.
  ///
  /// @return the `short` read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  short readShort() throws IOException;

  /// Reads an `int`.
  ///
  /// @return the `int` read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  int readInt() throws IOException;

  /// Reads a `long`.
  ///
  /// @return the `long` read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  long readLong() throws IOException;

  /// Reads a `float`.
  ///
  /// @return the `float` read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  float readFloat() throws IOException;

  /// Reads a `double`.
  ///
  /// @return the `double` read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  double readDouble() throws IOException;

  /// Reads an unsigned byte into a `short`.
  ///
  /// @return the unsigned byte read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  short readUByte() throws IOException;

  /// Reads an unsigned short into an `int`.
  ///
  /// @return the unsigned short read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  int readUShort() throws IOException;

  /// Reads an unsigned int into a `long`.
  ///
  /// @return the unsigned int read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  long readUInt() throws IOException;

  /// Reads an 8-bit unsigned length value into a `short`.
  ///
  /// This method is equivalent to [#readUByte()].
  ///
  /// @return the length value read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  short readLength8() throws IOException;

  /// Reads a 16-bit unsigned length value into an `int`.
  ///
  /// This method is equivalent to [#readUShort()].
  ///
  /// @return the length value read
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  int readLength16() throws IOException;

  /// Reads a 32-bit unsigned length value into an `int`.
  ///
  /// @param type the type of value whose length to read (used in exception message)
  /// @return the length value read
  /// @throws MxPackException.SizeLimitExceeded if the value is greater than [Integer#MAX_VALUE]
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  int readLength32(MessageType type) throws IOException;
}
