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
import org.minipack.java.internal.*;

/** The underlying sink of a {@link MessageWriter}. */
public interface MessageSink extends Closeable {
  static MessageSink of(WritableByteChannel channel) {
    return new DefaultMessageSink<>(new ChannelSinkProvider(channel));
  }

  static MessageSink of(WritableByteChannel channel, Consumer<Options> consumer) {
    return new DefaultMessageSink<>(new ChannelSinkProvider(channel), consumer);
  }

  static MessageSink of(OutputStream stream) {
    return new DefaultMessageSink<>(new StreamSinkProvider(stream));
  }

  static MessageSink of(OutputStream stream, Consumer<Options> consumer) {
    return new DefaultMessageSink<>(new StreamSinkProvider(stream), consumer);
  }

  static MessageSink ofDebug(ByteBuffer buffer) {
    return new DefaultMessageSink<>(new ErrorSinkProvider(), options -> {}, buffer);
  }

  static MessageSink ofDebug(ByteBuffer buffer, Consumer<Options> consumer) {
    return new DefaultMessageSink<>(new ErrorSinkProvider(), consumer, buffer);
  }

  /** Note: This sink performs buffer copying. */
  static MessageSink.InMemory<ByteBuffer> ofBuffer() {
    return new DefaultMessageSink<>(new BufferSinkProvider());
  }

  /** Note: This sink performs buffer copying. */
  static MessageSink.InMemory<ByteBuffer> ofBuffer(Consumer<Options> consumer) {
    return new DefaultMessageSink<>(new BufferSinkProvider(consumer), consumer);
  }

  static MessageSink of(Provider<Void> provider) {
    return new DefaultMessageSink<>(provider);
  }

  static MessageSink of(Provider<Void> provider, Consumer<Options> consumer) {
    return new DefaultMessageSink<>(provider, consumer);
  }

  static <T> MessageSink.InMemory<T> ofInMemory(Provider<T> provider) {
    return new DefaultMessageSink<>(provider);
  }

  static <T> MessageSink.InMemory<T> ofInMemory(Provider<T> provider, Consumer<Options> consumer) {
    return new DefaultMessageSink<>(provider, consumer);
  }

  interface Options {
    Options allocator(BufferAllocator allocator);

    @SuppressWarnings("UnusedReturnValue")
    Options bufferCapacity(int capacity);
  }

  interface InMemory<T> extends MessageSink {
    T output();
  }

  interface Provider<T> {
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
        write(buffer);
        bytesLeft -= bytesRead;
      }
      return length;
    }

    default T output() {
      throw Exceptions.notAnInMemorySink();
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

  /**
   * Writes enough bytes from the given buffer to this sink for {@linkplain ByteBuffer#put putting}
   * at least {@code length} bytes into the buffer.
   *
   * <p>The number of bytes written is between 0 and {@linkplain ByteBuffer#remaining() remaining}.
   */
  void ensureRemaining(int length) throws IOException;

  /**
   * Puts a byte value into the given buffer, ensuring that the buffer has enough space remaining.
   */
  void write(byte value) throws IOException;

  void write(byte[] values) throws IOException;

  void write(short value) throws IOException;

  void write(int value) throws IOException;

  void write(long value) throws IOException;

  void write(float value) throws IOException;

  void write(double value) throws IOException;

  /**
   * Puts two byte values into the given buffer, ensuring that the buffer has enough space
   * remaining.
   */
  void write(byte value1, byte value2) throws IOException;

  void write(byte value1, short value2) throws IOException;

  void write(byte value1, int value2) throws IOException;

  void write(byte value1, long value2) throws IOException;

  void write(byte value1, float value2) throws IOException;

  void write(byte value1, double value2) throws IOException;

  void flushBuffer() throws IOException;
}
