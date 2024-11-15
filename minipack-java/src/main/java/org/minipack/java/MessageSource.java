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

/** The underlying source of a {@link DefaultMessageReader}. */
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

    void skip(int length) throws IOException;

    void close() throws IOException;

    default long transferTo(WritableByteChannel channel, long maxBytesToTransfer)
        throws IOException {
      throw Exceptions.TODO();
    }
  }

  ByteBuffer buffer();

  BufferAllocator allocator();

  int read(ByteBuffer buffer) throws IOException;

  int readAtLeast(ByteBuffer buffer, int minBytes) throws IOException;

  long transferTo(WritableByteChannel destination, long maxBytesToTransfer) throws IOException;

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
