/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.minipack.java.internal.*;

/// The underlying sink of a [MessageWriter].
public interface MessageSink extends Closeable {
  static MessageSink of(WritableByteChannel channel) {
    return new DefaultMessageSink(new ChannelSinkProvider(channel));
  }

  static MessageSink of(WritableByteChannel channel, Consumer<Options> consumer) {
    return new DefaultMessageSink(new ChannelSinkProvider(channel), consumer);
  }

  static MessageSink of(OutputStream stream) {
    return new DefaultMessageSink(new StreamSinkProvider(stream));
  }

  static MessageSink of(OutputStream stream, Consumer<Options> consumer) {
    return new DefaultMessageSink(new StreamSinkProvider(stream), consumer);
  }

  static MessageSink ofDiscarding() {
    return new DefaultMessageSink(new DiscardingSinkProvider(), options -> {});
  }

  static MessageSink ofDiscarding(Consumer<Options> consumer) {
    return new DefaultMessageSink(new DiscardingSinkProvider(), consumer);
  }

  static MessageSink ofDiscarding(
      Consumer<Options> consumer, BufferAllocator.PooledByteBuffer sinkBuffer) {
    return new DefaultMessageSink(new DiscardingSinkProvider(), consumer, sinkBuffer);
  }

  /// Note: This sink performs buffer copying.
  static MessageSink.WithOutput<BufferAllocator.PooledByteBuffer> ofBuffer() {
    var output = new SinkOutput<BufferAllocator.PooledByteBuffer>();
    var sink = new DefaultMessageSink(new BufferSinkProvider(output));
    return new WithOutput<>(sink, output);
  }

  /// Note: This sink performs buffer copying.
  static MessageSink.WithOutput<BufferAllocator.PooledByteBuffer> ofBuffer(
      Consumer<Options> consumer) {
    var output = new SinkOutput<BufferAllocator.PooledByteBuffer>();
    var sink = new DefaultMessageSink(new BufferSinkProvider(output, consumer), consumer);
    return new WithOutput<>(sink, output);
  }

  static MessageSink of(Provider provider) {
    return new DefaultMessageSink(provider);
  }

  static MessageSink of(Provider provider, Consumer<Options> consumer) {
    return new DefaultMessageSink(provider, consumer);
  }

  interface Options {
    Options allocator(BufferAllocator allocator);

    @SuppressWarnings("UnusedReturnValue")
    Options bufferCapacity(int capacity);
  }

  record WithOutput<T>(MessageSink sink, Supplier<T> output) {}

  interface Provider {
    void write(ByteBuffer buffer) throws IOException;

    void write(ByteBuffer... buffers) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;

    default long transferFrom(ReadableByteChannel channel, final long length, ByteBuffer buffer)
        throws IOException {
      if (length == 0) return 0; // avoid writing buffer in this case
      write(buffer); // TODO: first fill?
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

  ByteBuffer buffer();

  BufferAllocator allocator();

  void write(ByteBuffer buffer) throws IOException;

  void write(ByteBuffer... buffers) throws IOException;

  long transferFrom(ReadableByteChannel channel, long length) throws IOException;

  void flush() throws IOException;

  @Override
  void close() throws IOException;

  /// Writes enough bytes from the given buffer to this sink for [putting][ByteBuffer#put]
  /// at least `length` bytes into the buffer.
  ///
  /// The number of bytes written is between 0 and [remaining][ByteBuffer#remaining()].
  void ensureRemaining(int length) throws IOException;

  /// Puts a byte value into the given buffer, ensuring that the buffer has enough space remaining.
  void write(byte value) throws IOException;

  void write(byte[] values) throws IOException;

  void write(short value) throws IOException;

  void write(int value) throws IOException;

  void write(long value) throws IOException;

  void write(float value) throws IOException;

  void write(double value) throws IOException;

  /// Puts two byte values into the given buffer, ensuring that the buffer has enough space
  /// remaining.
  void write(byte value1, byte value2) throws IOException;

  void write(byte value1, short value2) throws IOException;

  void write(byte value1, int value2) throws IOException;

  void write(byte value1, long value2) throws IOException;

  void write(byte value1, float value2) throws IOException;

  void write(byte value1, double value2) throws IOException;

  void flushBuffer() throws IOException;
}
