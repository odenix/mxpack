/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.minipack.core.internal.Exceptions;
import org.minipack.core.internal.MessageFormat;
import org.minipack.core.internal.RequestedType;

/**
 * Reads values encoded in the <a href="https://msgpack.org/">MessagePack</a> binary serialization
 * format.
 *
 * <p>To create a new {@code MessageReader}, use a {@linkplain #builder() builder}. To read a value,
 * call one of the {@code readXYZ()} methods. To determine the next value's type, call {@link
 * #nextType()}. To close this reader, call {@link #close()}.
 */
public final class MessageReader implements Closeable {
  private static final int MAX_IDENTIFIER_CACHE_SIZE = 1024 * 1024; // in bytes

  private final MessageSource source;
  private final MessageDecoder<String> stringDecoder;
  private final MessageDecoder<String> identifierDecoder;

  /** A builder of {@link MessageReader}. */
  public static final class Builder {
    private @Nullable MessageSource source;
    private @Nullable MessageDecoder<String> stringDecoder;
    private @Nullable MessageDecoder<String> identifierDecoder;

    private Builder() {}

    /** Sets the underlying source to read from. */
    public Builder source(MessageSource source) {
      this.source = source;
      return this;
    }

    /** Equivalent to {@code source(MessageSource.of(stream, allocator))}. */
    public Builder source(InputStream stream, BufferAllocator allocator) {
      source = MessageSource.of(stream, allocator);
      return this;
    }

    /** Equivalent to {@code source(MessageSource.of(channel, allocator))}. */
    public Builder source(ReadableByteChannel channel, BufferAllocator allocator) {
      source = MessageSource.of(channel, allocator);
      return this;
    }

    public Builder source(ByteBuffer buffer, BufferAllocator allocator) {
      source = MessageSource.of(buffer, allocator);
      return this;
    }

    public Builder stringDecoder(MessageDecoder<String> decoder) {
      stringDecoder = decoder;
      return this;
    }

    public Builder identifierDecoder(MessageDecoder<String> decoder) {
      identifierDecoder = decoder;
      return this;
    }

    /** Builds a {@code MessageReader} from this builder's current state. */
    public MessageReader build() {
      return new MessageReader(this);
    }
  }

  /** Creates a new {@code MessageReader} builder. */
  public static Builder builder() {
    return new Builder();
  }

  private MessageReader(Builder builder) {
    if (builder.source == null) {
      throw Exceptions.sourceRequired();
    }
    source = builder.source;
    stringDecoder =
        builder.stringDecoder != null
            ? builder.stringDecoder
            : MessageDecoder.stringDecoder(StandardCharsets.UTF_8.newDecoder());
    identifierDecoder =
        builder.identifierDecoder != null
            ? builder.identifierDecoder
            : MessageDecoder.identifierDecoder(
                StandardCharsets.UTF_8.newDecoder(), MAX_IDENTIFIER_CACHE_SIZE);
  }

  /** Returns the type of the next value to be read. */
  public MessageType nextType() throws IOException {
    return MessageFormat.toType(nextFormat());
  }

  public void skipValue() throws IOException {
    skipValue(1);
  }

  public void skipValue(int count) throws IOException {
    while (count-- > 0) {
      var format = source.readByte();
      switch (format) {
        case MessageFormat.NIL, MessageFormat.FALSE, MessageFormat.TRUE -> {}
        case MessageFormat.UINT8, MessageFormat.INT8 -> source.skip(1);
        case MessageFormat.UINT16, MessageFormat.INT16, MessageFormat.FIXEXT1 -> source.skip(2);
        case MessageFormat.UINT32, MessageFormat.INT32, MessageFormat.FLOAT32 -> source.skip(4);
        case MessageFormat.UINT64, MessageFormat.INT64, MessageFormat.FLOAT64 -> source.skip(8);
        case MessageFormat.FIXEXT2 -> source.skip(3);
        case MessageFormat.FIXEXT4 -> source.skip(5);
        case MessageFormat.FIXEXT8 -> source.skip(9);
        case MessageFormat.FIXEXT16 -> source.skip(17);
        case MessageFormat.EXT8 -> source.skip(source.readLength8() + 1);
        case MessageFormat.EXT16 -> source.skip(source.readLength16() + 1);
        case MessageFormat.EXT32 -> source.skip(source.readLength32(MessageType.EXTENSION) + 1);
        case MessageFormat.BIN8, MessageFormat.STR8 -> source.skip(source.readLength8());
        case MessageFormat.BIN16, MessageFormat.STR16 -> source.skip(source.readLength16());
        case MessageFormat.BIN32 -> source.skip(source.readLength32(MessageType.BINARY));
        case MessageFormat.STR32 -> source.skip(source.readLength32(MessageType.STRING));
        case MessageFormat.ARRAY16 -> count += source.readLength16();
        case MessageFormat.MAP16 -> count += source.readLength16() * 2;
        case MessageFormat.ARRAY32 -> count += source.readLength32(MessageType.ARRAY);
        case MessageFormat.MAP32 -> count += source.readLength32(MessageType.MAP) * 2;
        default -> {
          switch (MessageFormat.toType(format)) {
            case INTEGER -> {}
            case STRING -> source.skip(MessageFormat.getFixStrLength(format));
            case ARRAY -> count += MessageFormat.getFixArrayLength(format);
            case MAP -> count += MessageFormat.getFixMapLength(format) * 2;
            default -> throw Exceptions.invalidMessageFormat(format);
          }
        }
      }
    }
  }

  /** Reads a nil (null) value. */
  public void readNil() throws IOException {
    var format = source.readByte();
    if (format != MessageFormat.NIL) {
      throw Exceptions.typeMismatch(format, RequestedType.NIL);
    }
  }

  /** Reads a boolean value. */
  public boolean readBoolean() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.TRUE -> true;
      case MessageFormat.FALSE -> false;
      default -> throw Exceptions.typeMismatch(format, RequestedType.BOOLEAN);
    };
  }

  /** Reads an integer value that fits into a Java byte. */
  public byte readByte() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.INT8 -> source.readByte();
      case MessageFormat.INT16 -> {
        var value = source.readShort();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case MessageFormat.INT32 -> {
        var value = source.readInt();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case MessageFormat.INT64 -> {
        var value = source.readLong();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case MessageFormat.UINT8 -> {
        var value = source.readByte();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case MessageFormat.UINT16 -> {
        var value = source.readShort();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case MessageFormat.UINT32 -> {
        var value = source.readInt();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      case MessageFormat.UINT64 -> {
        var value = source.readLong();
        if (value >= 0 && value <= Byte.MAX_VALUE) yield (byte) value;
        throw Exceptions.integerOverflow(value, RequestedType.BYTE);
      }
      default -> {
        if (MessageFormat.isFixInt(format)) yield format;
        throw Exceptions.typeMismatch(format, RequestedType.BYTE);
      }
    };
  }

  /** Reads an integer value that fits into a Java short. */
  public short readShort() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.INT8 -> source.readByte();
      case MessageFormat.INT16 -> source.readShort();
      case MessageFormat.INT32 -> {
        var value = source.readInt();
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      case MessageFormat.INT64 -> {
        var value = source.readLong();
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      case MessageFormat.UINT8 -> source.readUByte();
      case MessageFormat.UINT16 -> {
        var value = source.readShort();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      case MessageFormat.UINT32 -> {
        var value = source.readInt();
        if (value >= 0 && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      case MessageFormat.UINT64 -> {
        var value = source.readLong();
        if (value >= 0 && value <= Short.MAX_VALUE) yield (short) value;
        throw Exceptions.integerOverflow(value, RequestedType.SHORT);
      }
      default -> {
        if (MessageFormat.isFixInt(format)) yield format;
        throw Exceptions.typeMismatch(format, RequestedType.SHORT);
      }
    };
  }

  /** Reads an integer value that fits into a Java int. */
  public int readInt() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.INT8 -> source.readByte();
      case MessageFormat.INT16 -> source.readShort();
      case MessageFormat.INT32 -> source.readInt();
      case MessageFormat.INT64 -> {
        var value = source.readLong();
        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) yield (int) value;
        throw Exceptions.integerOverflow(value, RequestedType.INT);
      }
      case MessageFormat.UINT8 -> source.readUByte();
      case MessageFormat.UINT16 -> source.readUShort();
      case MessageFormat.UINT32 -> {
        var value = source.readInt();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, RequestedType.INT);
      }
      case MessageFormat.UINT64 -> {
        var value = source.readLong();
        if (value >= 0 && value <= Integer.MAX_VALUE) yield (int) value;
        throw Exceptions.integerOverflow(value, RequestedType.INT);
      }
      default -> {
        if (MessageFormat.isFixInt(format)) yield format;
        throw Exceptions.typeMismatch(format, RequestedType.INT);
      }
    };
  }

  /** Reads an integer value that fits into a Java long. */
  public long readLong() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.INT8 -> source.readByte();
      case MessageFormat.INT16 -> source.readShort();
      case MessageFormat.INT32 -> source.readInt();
      case MessageFormat.INT64 -> source.readLong();
      case MessageFormat.UINT8 -> source.readUByte();
      case MessageFormat.UINT16 -> source.readUShort();
      case MessageFormat.UINT32 -> source.readUInt();
      case MessageFormat.UINT64 -> {
        var value = source.readLong();
        if (value >= 0) yield value;
        throw Exceptions.integerOverflow(value, RequestedType.LONG);
      }
      default -> {
        if (MessageFormat.isFixInt(format)) yield format;
        throw Exceptions.typeMismatch(format, RequestedType.LONG);
      }
    };
  }

  /** Reads a floating point value that fits into a Java float. */
  public float readFloat() throws IOException {
    var format = source.readByte();
    if (format == MessageFormat.FLOAT32) return source.readFloat();
    throw Exceptions.typeMismatch(format, RequestedType.FLOAT);
  }

  /** Reads a floating point value that fits into a Java double. */
  public double readDouble() throws IOException {
    var format = source.readByte();
    if (format == MessageFormat.FLOAT64) return source.readDouble();
    throw Exceptions.typeMismatch(format, RequestedType.DOUBLE);
  }

  /** Reads a timestamp value. */
  public Instant readTimestamp() throws IOException {
    var header = readExtensionHeader();
    if (!header.isTimestamp()) {
      throw Exceptions.timestampTypeMismatch(header.type());
    }
    return switch (header.length()) {
      case 4 -> Instant.ofEpochSecond(source.readInt() & 0xffffffffL);
      case 8 -> {
        var value = source.readLong();
        var nanos = value >>> 34;
        var seconds = value & 0x3ffffffffL;
        yield Instant.ofEpochSecond(seconds, nanos);
      }
      case 12 -> {
        var nanos = source.readInt();
        var seconds = source.readLong();
        yield Instant.ofEpochSecond(seconds, nanos);
      }
      default -> throw Exceptions.invalidTimestampLength(header.length());
    };
  }

  /**
   * Reads a string value.
   *
   * <p>To read a string as a sequence of bytes, use {@link #readStringHeader()} together with
   * {@link #readPayload}.
   */
  public String readString() throws IOException {
    return stringDecoder.decode(source, this);
  }

  public String readIdentifier() throws IOException {
    return identifierDecoder.decode(source, this);
  }

  public <T> T read(MessageDecoder<T> decoder) throws IOException {
    return decoder.decode(source, this);
  }

  /**
   * Starts reading an array value.
   *
   * <p>A call to this method <i>must</i> be followed by {@code n} calls that read the array's
   * elements, where {@code n} is the number of array elements returned by this method.
   */
  public int readArrayHeader() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.ARRAY16 -> source.readLength16();
      case MessageFormat.ARRAY32 -> source.readLength32(MessageType.ARRAY);
      default -> {
        if (MessageFormat.isFixArray(format)) {
          yield MessageFormat.getFixArrayLength(format);
        }
        throw Exceptions.typeMismatch(format, RequestedType.ARRAY);
      }
    };
  }

  /**
   * Starts reading a map value.
   *
   * <p>A call to this method <i>must</i> be followed by {@code n*2} calls that alternately read the
   * map's keys and values, where {@code n} is the number of map entries returned by this method.
   */
  public int readMapHeader() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.MAP16 -> source.readLength16();
      case MessageFormat.MAP32 -> source.readLength32(MessageType.MAP);
      default -> {
        if (MessageFormat.isFixMap(format)) {
          yield MessageFormat.getFixMapLength(format);
        }
        throw Exceptions.typeMismatch(format, RequestedType.MAP);
      }
    };
  }

  /**
   * Starts reading a binary value.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   */
  public int readBinaryHeader() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.BIN8 -> source.readLength8();
      case MessageFormat.BIN16 -> source.readLength16();
      case MessageFormat.BIN32 -> source.readLength32(MessageType.BINARY);
      default -> throw Exceptions.typeMismatch(format, RequestedType.BINARY);
    };
  }

  /**
   * Starts reading a string value as a sequence of bytes.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   *
   * <p>This method is a low-level alternative to {@link #readString()}.
   */
  public int readStringHeader() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.STR8 -> source.readLength8();
      case MessageFormat.STR16 -> source.readLength16();
      case MessageFormat.STR32 -> source.readLength32(MessageType.STRING);
      default -> {
        if (MessageFormat.isFixStr(format)) {
          yield MessageFormat.getFixStrLength(format);
        }
        throw Exceptions.typeMismatch(format, RequestedType.STRING);
      }
    };
  }

  /**
   * Starts reading an extension value.
   *
   * <p>A call to this method <i>must</i> be followed by a call to {@link #readPayload}.
   */
  public ExtensionHeader readExtensionHeader() throws IOException {
    var format = source.readByte();
    return switch (format) {
      case MessageFormat.FIXEXT1 -> new ExtensionHeader(1, source.readByte());
      case MessageFormat.FIXEXT2 -> new ExtensionHeader(2, source.readByte());
      case MessageFormat.FIXEXT4 -> new ExtensionHeader(4, source.readByte());
      case MessageFormat.FIXEXT8 -> new ExtensionHeader(8, source.readByte());
      case MessageFormat.FIXEXT16 -> new ExtensionHeader(16, source.readByte());
      case MessageFormat.EXT8 -> new ExtensionHeader(source.readLength8(), source.readByte());
      case MessageFormat.EXT16 -> new ExtensionHeader(source.readLength16(), source.readByte());
      case MessageFormat.EXT32 ->
          new ExtensionHeader(source.readLength32(MessageType.EXTENSION), source.readByte());
      default -> throw Exceptions.typeMismatch(format, RequestedType.EXTENSION);
    };
  }

  public void readPayload(ByteBuffer destination) throws IOException {
    var buffer = source.buffer();
    if (buffer.remaining() > 0) {
      var transferLength = Math.min(buffer.remaining(), destination.remaining());
      destination.put(destination.position(), buffer, buffer.position(), transferLength);
      buffer.position(buffer.position() + transferLength);
      destination.position(destination.position() + transferLength);
    }
    source.read(destination);
  }

  public void readPayload(WritableByteChannel destination, long maxBytes) throws IOException {
    source.transferTo(destination, maxBytes);
  }

  public void readPayload(OutputStream destination, long maxBytes) throws IOException {
    source.transferTo(Channels.newChannel(destination), maxBytes);
  }

  /** Closes the underlying message {@linkplain MessageSource source}. */
  @Override
  public void close() throws IOException {
    source.close();
  }

  // non-private for testing
  byte nextFormat() throws IOException {
    return source.nextByte();
  }
}
