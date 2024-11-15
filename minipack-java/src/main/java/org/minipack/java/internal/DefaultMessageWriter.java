/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.Instant;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.minipack.java.ExtensionHeader;
import org.minipack.java.MessageEncoder;
import org.minipack.java.MessageSink;
import org.minipack.java.MessageWriter;

/** Default implementation of {@link MessageWriter}. */
public final class DefaultMessageWriter implements MessageWriter {
  private static final int MAX_IDENTIFIER_CACHE_SIZE = 1024 * 1024; // in bytes

  private final MessageSink sink;
  private final MessageEncoder<CharSequence> stringEncoder;
  private final MessageEncoder<? super String> identifierEncoder;

  private static final class DefaultOptions implements Options {
    private @Nullable MessageEncoder<CharSequence> stringEncoder;
    private @Nullable MessageEncoder<? super String> identifierEncoder;

    public DefaultOptions stringEncoder(MessageEncoder<CharSequence> encoder) {
      stringEncoder = encoder;
      return this;
    }

    public DefaultOptions identifierEncoder(MessageEncoder<? super String> encoder) {
      identifierEncoder = encoder;
      return this;
    }
  }

  public DefaultMessageWriter(MessageSink sink) {
    this(sink, (options) -> {});
  }

  public DefaultMessageWriter(MessageSink sink, Consumer<Options> consumer) {
    this.sink = sink;
    var options = new DefaultOptions();
    consumer.accept(options);
    stringEncoder =
        options.stringEncoder != null ? options.stringEncoder : MessageEncoder.ofStrings();
    identifierEncoder =
        options.identifierEncoder != null
            ? options.identifierEncoder
            : MessageEncoder.ofIdentifiers();
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
    stringEncoder.encode(string, sink, this);
  }

  @Override
  public void writeIdentifier(String identifier) throws IOException {
    identifierEncoder.encode(identifier, sink, this);
  }

  @Override
  public <T> void write(T value, MessageEncoder<T> encoder) throws IOException {
    encoder.encode(value, sink, this);
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
  public void writePayload(ByteBuffer source) throws IOException {
    sink.write(source);
  }

  @Override
  public void writePayloads(ByteBuffer... sources) throws IOException {
    sink.write(sources);
  }

  @Override
  public long writePayload(ReadableByteChannel source, long maxBytes) throws IOException {
    return sink.transferFrom(source, maxBytes);
  }

  @Override
  public long writePayload(InputStream source, long maxBytes) throws IOException {
    return sink.transferFrom(Channels.newChannel(source), maxBytes);
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

  private void checkLength(int length) {
    if (length < 0) throw Exceptions.negativeLength(length);
  }
}
