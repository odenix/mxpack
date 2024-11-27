/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.minipack.core.internal.*;

/// A destination of bytes that a [MessageWriter] writes to.
public sealed interface MessageSink permits MessageSinkImpl {
  /// Adapts a [MessageSink] to an I/O abstraction such as [WritableByteChannel] or [OutputStream].
  interface Provider {
    /// Writes the [remaining][ByteBuffer#remaining()] bytes of the given byte buffer to the underlying I/O abstraction.
    ///
    /// @param buffer the byte buffer to read from
    /// @throws IOException if an I/O error occurs
    void write(ByteBuffer buffer) throws IOException;

    /// Writes the [remaining][ByteBuffer#remaining()] bytes of the given byte buffers to the underlying I/O abstraction.
    ///
    /// @param buffers the byte buffers to read from
    /// @throws IOException if an I/O error occurs
    void write(ByteBuffer... buffers) throws IOException;

    /// Flushes the underlying I/O abstraction.
    ///
    /// @throws IOException if an I/O error occurs
    void flush() throws IOException;

    /// Closes the underlying I/O abstraction.
    ///
    /// This method is called at most once.
    /// No other method is called after this method.
    ///
    /// @throws IOException if an I/O error occurs
    void close() throws IOException;

    /// Transfers up to `length` bytes from the given channel to the underlying I/O abstraction.
    ///
    /// Fewer than `length` bytes may be transferred if the channel's end of input is reached.
    ///
    /// @param channel the channel to transfer bytes from
    /// @param length the number of bytes to transfer
    /// @param buffer A buffer whose remaining bytes must be transferred before transferring
    ///               `length` bytes from the given channel.
    ///               The buffer can also be used for transferring bytes from the channel.
    /// @return the actual number of bytes transferred (between 0 and `length`)
    /// @throws IOException if an I/O error occurs
    default long transferFrom(ReadableByteChannel channel, long length, ByteBuffer buffer)
        throws IOException {
      if (length == 0) return 0; // avoid writing buffer in this case
      write(buffer);
      var bytesLeft = length;
      var capacity = buffer.capacity();
      while (bytesLeft > 0) {
        buffer.position(0).limit((int) Math.min(bytesLeft, capacity));
        var bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
          return length - bytesLeft;
        }
        buffer.flip();
        write(buffer);
        bytesLeft -= bytesRead;
      }
      return length;
    }
  }

  /// Returns the write buffer of this messge sink.
  ///
  /// @return the write buffer of this message sink
  ByteBuffer buffer();

  /// Returns the buffer allocator used by this message sink.
  ///
  /// @return the byte allocator used by this message sink
  BufferAllocator allocator();

  /// Writes the [remaining][ByteBuffer#remaining] bytes of the given buffer.
  ///
  /// This method blocks until the given buffer has been written.
  ///
  /// @param buffer the buffer to write
  /// @throws IOException if an I/O error occurs
  void write(ByteBuffer buffer) throws IOException;

  /// Writes the [remaining][ByteBuffer#remaining] bytes of the given buffers.
  ///
  /// This method blocks until the given buffers have been written.
  /// Calling this method may be more efficient than repeatedly calling [#write(ByteBuffer)].
  ///
  /// @param buffers the buffers to write
  /// @throws IOException if an I/O error occurs
  void write(ByteBuffer... buffers) throws IOException;

  /// Transfers up to `length` bytes from the given channel to this message sink.
  ///
  /// Fewer than `length` bytes may be transferred if the channel's end of input is reached.
  ///
  /// @param channel the channel to read from
  /// @param length the number of bytes to transfer
  /// @return the actual number of bytes transferred (between 0 and `length`)
  /// @throws IOException if an I/O error occurs
  long transferFrom(ReadableByteChannel channel, long length) throws IOException;

  /// Ensures that the [buffer][#buffer()] of this message sink
  /// has at least `length` bytes [remaining][ByteBuffer#remaining()].
  ///
  /// If the buffer has fewer than `length` bytes remaining,
  /// this method blocks until enough bytes have been written.
  ///
  /// @param length the minimum number of bytes that the buffer needs to have remaining
  /// @throws IOException if an I/O error occurs
  void ensureRemaining(int length) throws IOException;

  /// Writes a `byte`.
  ///
  /// @param value the value to write
  /// @throws IOException if an I/O error occurs
  void write(byte value) throws IOException;

  ///  Writes a `short`.
  ///
  /// @param value the value to write
  /// @throws IOException if an I/O error occurs
  void write(short value) throws IOException;

  /// Writes an `int`.
  ///
  /// @param value the value to write
  /// @throws IOException if an I/O error occurs
  void write(int value) throws IOException;

  /// Writes a `long`.
  ///
  /// @param value the value to write
  /// @throws IOException if an I/O error occurs
  void write(long value) throws IOException;

  ///  Writes a `float`.
  ///
  /// @param value the value to write
  /// @throws IOException if an I/O error occurs
  void write(float value) throws IOException;

  ///  Writes a `double`.
  ///
  /// @param value the value to write
  /// @throws IOException if an I/O error occurs
  void write(double value) throws IOException;

  /// Writes a `byte` followed by a `byte`.
  ///
  /// @param value1 the first value to write
  /// @param value2 the second value to write
  /// @throws IOException if an I/O error occurs
  void write(byte value1, byte value2) throws IOException;

  /// Writes a `byte` followed by a `short`.
  ///
  /// @param value1 the first value to write
  /// @param value2 the second value to write
  /// @throws IOException if an I/O error occurs
  void write(byte value1, short value2) throws IOException;

  /// Writes a `byte` followed by an `int`.
  ///
  /// @param value1 the first value to write
  /// @param value2 the second value to write
  /// @throws IOException if an I/O error occurs
  void write(byte value1, int value2) throws IOException;

  /// Writes a `byte` followed by a `long`.
  ///
  /// @param value1 the first value to write
  /// @param value2 the second value to write
  /// @throws IOException if an I/O error occurs
  void write(byte value1, long value2) throws IOException;

  /// Writes a `byte` followed by a `float`.
  ///
  /// @param value1 the first value to write
  /// @param value2 the second value to write
  /// @throws IOException if an I/O error occurs
  void write(byte value1, float value2) throws IOException;

  ///  Writes a `byte` followed by a `double`.
  ///
  /// @param value1 the first value to write
  /// @param value2 the second value to write
  /// @throws IOException if an I/O error occurs
  void write(byte value1, double value2) throws IOException;

  ///  Flushes the [buffer][#buffer()] of this message sink.
  ///
  /// @throws IOException if an I/O error occurs
  void flushBuffer() throws IOException;

  /// Flushes this message sink.
  ///
  /// This method calls [#flushBuffer()] and [Provider#flush()].
  ///
  /// @throws IOException if an I/O error occurs
  void flush() throws IOException;
}
