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

  static MessageSink.InMemory<ByteBuffer> ofBuffer() {
    return new DefaultMessageSink<>(new BufferSinkProvider());
  }

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

    default long transferFrom(ReadableByteChannel channel, long maxBytesToTransfer)
        throws IOException {
      throw Exceptions.TODO();
    }

    void flush() throws IOException;

    void close() throws IOException;

    default T output() {
      // TODO: move to Exceptions
      throw new UnsupportedOperationException();
    }
  }

  ByteBuffer buffer();

  BufferAllocator allocator();

  void write(ByteBuffer buffer) throws IOException;

  void write(ByteBuffer... buffers) throws IOException;

  long transferFrom(ReadableByteChannel channel, long maxBytesToTransfer) throws IOException;

  void flush() throws IOException;

  @Override
  void close() throws IOException;

  void ensureRemaining(int byteCount) throws IOException;

  void write(byte value) throws IOException;

  void write(byte value1, byte value2) throws IOException;

  void write(byte value1, byte value2, byte value3) throws IOException;

  void write(byte value1, byte value2, byte value3, byte value4) throws IOException;

  void write(byte[] values) throws IOException;

  void write(short value) throws IOException;

  void write(int value) throws IOException;

  void write(long value) throws IOException;

  void write(float value) throws IOException;

  void write(double value) throws IOException;

  void write(byte value1, short value2) throws IOException;

  void write(byte value1, int value2) throws IOException;

  void write(byte value1, long value2) throws IOException;

  void write(byte value1, float value2) throws IOException;

  void write(byte value1, double value2) throws IOException;

  void flushBuffer() throws IOException;
}
