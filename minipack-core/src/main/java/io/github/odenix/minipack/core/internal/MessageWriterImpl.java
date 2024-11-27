/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.internal;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;
import java.util.function.Consumer;

import io.github.odenix.minipack.core.*;

/// Implementation of [MessageWriter].
public final class MessageWriterImpl implements MessageWriter {
  private final MessageSinkImpl sink;
  private final MessageEncoder<CharSequence> stringEncoder;
  private final MessageEncoder<? super String> identifierEncoder;

  public static final class OptionBuilderImpl implements OptionBuilder {
    private BufferAllocator allocator = BufferAllocator.ofUnpooled();
    private int writeBufferCapacity = 1024 * 8;
    private MessageEncoder<CharSequence> stringEncoder = MessageEncoder.ofStrings();
    private MessageEncoder<? super String> identifierEncoder = MessageEncoder.ofStrings();

    private OptionBuilderImpl() {}

    @Override
    public OptionBuilder allocator(BufferAllocator allocator) {
      this.allocator = allocator;
      return this;
    }

    @Override
    public OptionBuilder writeBufferCapacity(int capacity) {
      if (capacity < MessageSinkImpl.MIN_BUFFER_CAPACITY) {
        throw Exceptions.bufferTooSmall(capacity, MessageSinkImpl.MIN_BUFFER_CAPACITY);
      }
      writeBufferCapacity = capacity;
      return this;
    }

    @Override
    public OptionBuilder stringEncoder(MessageEncoder<CharSequence> encoder) {
      stringEncoder = encoder;
      return this;
    }

    @Override
    public OptionBuilder identifierEncoder(MessageEncoder<? super String> encoder) {
      identifierEncoder = encoder;
      return this;
    }
  }

  public static MessageWriter of(WritableByteChannel channel, Consumer<OptionBuilder> optionHandler) {
    return of(new ChannelSinkProvider(channel), optionHandler);
  }

  public static MessageWriter of(OutputStream stream, Consumer<OptionBuilder> optionHandler) {
    return of(new StreamSinkProvider(stream), optionHandler);
  }

  public static MessageWriter of(MessageSink.Provider provider, Consumer<OptionBuilder> optionHandler) {
    var options = new OptionBuilderImpl();
    optionHandler.accept(options);
    var buffer = options.allocator.getByteBuffer(options.writeBufferCapacity);
    var sink = new MessageSinkImpl(provider, options.allocator, buffer);
    return new MessageWriterImpl(sink, options.stringEncoder, options.identifierEncoder);
  }

  public static MessageWriter of(BufferOutput output, Consumer<OptionBuilder> optionHandler) {
    var options = new OptionBuilderImpl();
    optionHandler.accept(options);
    var provider = new BufferSinkProvider(output, options.allocator);
    var buffer = options.allocator.getByteBuffer(options.writeBufferCapacity);
    var sink = new MessageSinkImpl(provider, options.allocator, buffer);
    return new MessageWriterImpl(sink, options.stringEncoder, options.identifierEncoder);
  }

  public static MessageWriter ofDiscarding() {
    return of(new DiscardingSinkProvider(), options ->
        options.writeBufferCapacity(MessageSinkImpl.MIN_BUFFER_CAPACITY));
  }

  public static MessageWriter ofDiscarding(ByteBuffer buffer) {
    var options = new OptionBuilderImpl();
    var provider = new DiscardingSinkProvider();
    var leasedBuffer = new LeasedByteBufferImpl(buffer, null);
    var sink = new MessageSinkImpl(provider, options.allocator, leasedBuffer);
    return new MessageWriterImpl(sink, options.stringEncoder, options.identifierEncoder);
  }

  private MessageWriterImpl(MessageSinkImpl sink,
                            MessageEncoder<CharSequence> stringEncoder,
                            MessageEncoder<? super String> identifierEncoder) {
    this.sink = sink;
    this.stringEncoder = stringEncoder;
    this.identifierEncoder = identifierEncoder;
  }

  @Override
  public void writeNil() throws IOException {
    sink.write(MessageFormat.NIL);
  }

  @Override
  public void write(boolean value) throws IOException {
    sink.write(value ? MessageFormat.TRUE : MessageFormat.FALSE);
  }

  @Override
  public void write(byte value) throws IOException {
    if (value < -(1 << 5)) {
      writeInt8(value);
    } else {
      sink.write(value);
    }
  }

  @Override
  public void write(short value) throws IOException {
    if (value < -(1 << 5)) {
      if (value < -(1 << 7)) {
        writeInt16(value);
      } else {
        writeInt8((byte) value);
      }
    } else if (value < (1 << 7)) {
      sink.write((byte) value);
    } else {
      if (value < (1 << 8)) {
        writeUInt8((byte) value);
      } else {
        writeUInt16(value);
      }
    }
  }

  @Override
  public void write(int value) throws IOException {
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
      sink.write((byte) value);
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

  @Override
  public void write(long value) throws IOException {
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
      sink.write((byte) value);
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

  @Override
  public void write(float value) throws IOException {
    writeFloat32(value);
  }

  @Override
  public void write(double value) throws IOException {
    writeFloat64(value);
  }

  @Override
  public void write(Instant value) throws IOException {
    var seconds = value.getEpochSecond();
    var nanos = value.getNano();
    if (nanos == 0 && seconds >= 0 && seconds < (1L << 32)) {
      writeExtensionHeader(4, ExtensionHeader.TIMESTAMP_TYPE);
      sink.write((int) seconds);
    } else if (seconds >= 0 && seconds < (1L << 34)) {
      writeExtensionHeader(8, ExtensionHeader.TIMESTAMP_TYPE);
      sink.write(((long) nanos) << 34 | seconds);
    } else {
      writeExtensionHeader(12, ExtensionHeader.TIMESTAMP_TYPE);
      sink.write(nanos);
      sink.write(seconds);
    }
  }

  @Override
  public void write(CharSequence string) throws IOException {
    stringEncoder.encode(string, sink);
  }

  @Override
  public void writeIdentifier(String identifier) throws IOException {
    identifierEncoder.encode(identifier, sink);
  }

  @Override
  public <T> void write(T value, MessageEncoder<T> encoder) throws IOException {
    encoder.encode(value, sink);
  }

  @Override
  public void writeArrayHeader(int elementCount) throws IOException {
    checkLength(elementCount);
    if (elementCount < (1 << 4)) {
      sink.write((byte) (MessageFormat.FIXARRAY_PREFIX | elementCount));
    } else if (elementCount < (1 << 16)) {
      sink.write(MessageFormat.ARRAY16, (short) elementCount);
    } else {
      sink.write(MessageFormat.ARRAY32, elementCount);
    }
  }

  @Override
  public void writeMapHeader(int entryCount) throws IOException {
    checkLength(entryCount);
    if (entryCount < (1 << 4)) {
      sink.write((byte) (MessageFormat.FIXMAP_PREFIX | entryCount));
    } else if (entryCount < (1 << 16)) {
      sink.write(MessageFormat.MAP16, (short) entryCount);
    } else {
      sink.write(MessageFormat.MAP32, entryCount);
    }
  }

  @Override
  public void writeStringHeader(int length) throws IOException {
    checkLength(length);
    if (length < (1 << 5)) {
      sink.write((byte) (MessageFormat.FIXSTR_PREFIX | length));
    } else if (length < (1 << 8)) {
      sink.write(MessageFormat.STR8, (byte) length);
    } else if (length < (1 << 16)) {
      sink.write(MessageFormat.STR16, (short) length);
    } else {
      sink.write(MessageFormat.STR32, length);
    }
  }

  @Override
  public void writeBinaryHeader(int length) throws IOException {
    checkLength(length);
    if (length < (1 << 8)) {
      sink.write(MessageFormat.BIN8, (byte) length);
    } else if (length < (1 << 16)) {
      sink.write(MessageFormat.BIN16, (short) length);
    } else {
      sink.write(MessageFormat.BIN32, length);
    }
  }

  @Override
  public void writeExtensionHeader(int length, byte type) throws IOException {
    checkLength(length);
    switch (length) {
      case 1 -> sink.write(MessageFormat.FIXEXT1, type);
      case 2 -> sink.write(MessageFormat.FIXEXT2, type);
      case 4 -> sink.write(MessageFormat.FIXEXT4, type);
      case 8 -> sink.write(MessageFormat.FIXEXT8, type);
      case 16 -> sink.write(MessageFormat.FIXEXT16, type);
      default -> {
        if (length < (1 << 8)) {
          sink.write(MessageFormat.EXT8, (byte) length);
        } else if (length < (1 << 16)) {
          sink.write(MessageFormat.EXT16, (short) length);
        } else {
          sink.write(MessageFormat.EXT32, length);
        }
        sink.write(type);
      }
    }
  }

  @Override
  public void writePayload(ByteBuffer buffer) throws IOException {
    sink.write(buffer);
  }

  @Override
  public void writePayload(ByteBuffer... buffers) throws IOException {
    sink.write(buffers);
  }

  @Override
  public long writePayload(ReadableByteChannel channel, long length) throws IOException {
    return sink.transferFrom(channel, length);
  }

  @Override
  public long writePayload(InputStream stream, long length) throws IOException {
    return sink.transferFrom(Channels.newChannel(stream), length);
  }

  public void writeUnsigned(byte value) throws IOException {
    if (value < 0) {
      writeUInt8(value);
    } else {
      sink.write(value);
    }
  }

  public void writeUnsigned(short value) throws IOException {
    if (value < 0) {
      writeUInt16(value);
    } else if (value < (1 << 7)) {
      sink.write((byte) value);
    } else {
      if (value < (1 << 8)) {
        writeUInt8((byte) value);
      } else {
        writeUInt16(value);
      }
    }
  }

  public void writeUnsigned(int value) throws IOException {
    if (value < 0) {
      writeUInt32(value);
    } else if (value < (1 << 7)) {
      sink.write((byte) value);
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

  public void writeUnsigned(long value) throws IOException {
    if (value < 0) {
      writeUInt64(value);
    } else if (value < (1 << 7)) {
      sink.write((byte) value);
    } else {
      if (value < (1L << 32)) {
        if (value < (1 << 16)) {
          if (value < (1 << 8)) {
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

  @Override
  public void flush() throws IOException {
    sink.flush();
  }

  @Override
  public void close() throws IOException {
    sink.close();
  }

  private void writeInt8(byte value) throws IOException {
    sink.write(MessageFormat.INT8, value);
  }

  private void writeUInt8(byte value) throws IOException {
    sink.write(MessageFormat.UINT8, value);
  }

  private void writeInt16(short value) throws IOException {
    sink.write(MessageFormat.INT16, value);
  }

  private void writeUInt16(short value) throws IOException {
    sink.write(MessageFormat.UINT16, value);
  }

  private void writeInt32(int value) throws IOException {
    sink.write(MessageFormat.INT32, value);
  }

  private void writeUInt32(int value) throws IOException {
    sink.write(MessageFormat.UINT32, value);
  }

  private void writeInt64(long value) throws IOException {
    sink.write(MessageFormat.INT64, value);
  }

  private void writeUInt64(long value) throws IOException {
    sink.write(MessageFormat.UINT64, value);
  }

  private void writeFloat32(float value) throws IOException {
    sink.write(MessageFormat.FLOAT32, value);
  }

  private void writeFloat64(double value) throws IOException {
    sink.write(MessageFormat.FLOAT64, value);
  }

  private static void checkLength(int length) {
    if (length < 0) throw Exceptions.negativeArgument(length);
  }
}
