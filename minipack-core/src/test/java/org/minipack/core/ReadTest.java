/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.api.lifecycle.AfterTry;
import net.jqwik.api.lifecycle.BeforeTry;
import org.minipack.core.internal.MessageReaderImpl;
import org.minipack.core.internal.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

/// Tests [MessageReader] against [org.msgpack.core.MessagePacker].
@SuppressWarnings("unused")
abstract sealed class ReadTest {
  private final BufferAllocator allocator;
  private final boolean isChannel;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessagePacker packer;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessageReader reader;

  static final class InputStreamHeapBufferTest extends ReadTest {
    InputStreamHeapBufferTest() {
      super(false, false);
    }
  }

  static final class ChannelHeapBufferTest extends ReadTest {
    ChannelHeapBufferTest() {
      super(true, false);
    }
  }

  static final class ChannelDirectBufferTest extends ReadTest {
    ChannelDirectBufferTest() {
      super(true, true);
    }
  }

  ReadTest(boolean isChannel, boolean isDirect) {
    this.isChannel = isChannel;
    allocator = isDirect
        ? BufferAllocator.ofPooled(options -> options.preferDirectBuffers(true))
        : BufferAllocator.ofUnpooled();
  }

  @BeforeTry
  void beforeTry() throws IOException {
    var in = new PipedInputStream(1 << 19);
    var out = new PipedOutputStream(in);
    packer = MessagePack.newDefaultPacker(out);
    reader =  isChannel
        ? MessageReader.of(
            Channels.newChannel(in),
            options -> options.allocator(allocator).readBufferCapacity(1 << 8))
        : MessageReader.of(in, options -> options.allocator(allocator).readBufferCapacity(1 << 8));
  }

  @AfterTry
  void afterTry() throws IOException {
    reader.close();
  }

  @AfterProperty
  void afterProperty() {
    allocator.close();
  }

  @Example
  void Nil() throws IOException {
    packer.packNil().flush();
    packer.close();
    assertThat(nextFormat()).isEqualTo(MessageFormat.NIL);
    assertThat(reader.nextType()).isEqualTo(MessageType.NIL);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  void Boolean(@ForAll boolean input) throws IOException {
    packer.packBoolean(input).flush();
    packer.close();
    assertThat(nextFormat()).isEqualTo(input ? MessageFormat.TRUE : MessageFormat.FALSE);
    assertThat(reader.nextType()).isEqualTo(MessageType.BOOLEAN);
    var output = reader.readBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Byte(@ForAll byte input) throws IOException {
    packer.packByte(input).flush();
    packer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.INT8),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT8));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readByte();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Short(@ForAll short input) throws IOException {
    packer.packShort(input).flush();
    packer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.INT8),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT8),
            format -> assertThat(format).isEqualTo(MessageFormat.INT16),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT16));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readShort();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Int(@ForAll int input) throws IOException {
    packer.packInt(input).flush();
    packer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.INT8),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT8),
            format -> assertThat(format).isEqualTo(MessageFormat.INT16),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT16),
            format -> assertThat(format).isEqualTo(MessageFormat.INT32),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT32));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readInt();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Long(@ForAll long input) throws IOException {
    packer.packLong(input).flush();
    packer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.INT8),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT8),
            format -> assertThat(format).isEqualTo(MessageFormat.INT16),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT16),
            format -> assertThat(format).isEqualTo(MessageFormat.INT32),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT32),
            format -> assertThat(format).isEqualTo(MessageFormat.INT64),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT64));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readLong();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Float(@ForAll float input) throws IOException {
    packer.packFloat(input).flush();
    packer.close();
    assertThat(nextFormat()).isEqualTo(MessageFormat.FLOAT32);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Double(@ForAll double input) throws IOException {
    packer.packDouble(input).flush();
    packer.close();
    assertThat(nextFormat()).isEqualTo(MessageFormat.FLOAT64);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readDouble();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Timestamp(@ForAll Instant input) throws IOException {
    packer.packTimestamp(input).flush();
    packer.close();
    assertThat(reader.nextType()).isEqualTo(MessageType.EXTENSION);
    var output = reader.readTimestamp();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void AsciiString(@ForAll @CharRange(to = 127) String input) throws IOException {
    writeAndReadString(input);
  }

  @Property
  void String(@ForAll String input) throws IOException {
    writeAndReadString(input);
  }

  @Property
  void LongAsciiString(
      @ForAll @CharRange(to = 127) @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    writeAndReadString(input);
  }

  @Property
  void LongString(@ForAll @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    writeAndReadString(input);
  }

  @Property
  void Identifier(@ForAll @StringLength(max = 1 << 6) String input) throws IOException {
    packer.packString(input);
    packer.flush();
    packer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixStr(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.STR8),
            format -> assertThat(format).isEqualTo(MessageFormat.STR16),
            format -> assertThat(format).isEqualTo(MessageFormat.STR32));
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readIdentifier();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void RawString(@ForAll String input) throws IOException {
    packer.packString(input).flush();
    packer.close();
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var length = reader.readStringHeader();
    var buffer = ByteBuffer.allocate(length);
    reader.readPayload(buffer);
    var output = new String(buffer.array(), StandardCharsets.UTF_8);
    assertThat(output).isEqualTo(input);
  }

  @Property
  void BinaryIntoByteBuffer(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var buffer = ByteBuffer.allocate(length);
    while (buffer.hasRemaining()) reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(input).isEqualTo(output);
  }

  @Property
  void BinaryIntoMultipleByteBuffers1(@ForAll byte[] input1, @ForAll byte[] input2)
      throws IOException {
    var length = input1.length + input2.length;
    var input = ByteBuffer.allocate(length).put(input1).put(input2);
    writeBinaryAndReadHeader(input.array());
    var buffer1 = ByteBuffer.allocate(input1.length);
    while (buffer1.hasRemaining()) reader.readPayload(buffer1);
    assertThat(buffer1.array()).isEqualTo(input1);
    var buffer2 = ByteBuffer.allocate(input2.length);
    while (buffer2.hasRemaining()) reader.readPayload(buffer2);
    assertThat(buffer2.array()).isEqualTo(input2);
  }

  @Property
  void BinaryIntoOutputStream(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var outputStream = new ByteArrayOutputStream(length);
    reader.readPayload(outputStream, length);
    var output = outputStream.toByteArray();
    assertThat(input).isEqualTo(output);
  }

  @Property
  void BinaryIntoChannel(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var outputStream = new ByteArrayOutputStream(length);
    reader.readPayload(Channels.newChannel(outputStream), length);
    var output = outputStream.toByteArray();
    assertThat(input).isEqualTo(output);
  }

  @Property(tries = 10)
  void BinaryIntoChannelFromFileChannelSource(@ForAll byte[] input) throws IOException {
    var inputFile = Files.createTempFile(null, null);
    try (var allocator = BufferAllocator.ofUnpooled();
        var packer = MessagePack.newDefaultPacker(Files.newOutputStream(inputFile));
        var reader = MessageReader.of(
            FileChannel.open(inputFile),
            options -> options.allocator(allocator).readBufferCapacity(1 << 8))) {
      var length = writeBinaryAndReadHeader(input, packer, reader);
      var outputStream = new ByteArrayOutputStream(length);
      reader.readPayload(Channels.newChannel(outputStream), length);
      var output = outputStream.toByteArray();
      assertThat(input).isEqualTo(output);
    } finally {
      Files.delete(inputFile);
    }
  }

  @Property(tries = 10)
  void BinaryIntoFileChannel(@ForAll byte[] input) throws IOException {
    var outputFile = Files.createTempFile(null, null);
    try (var channel = FileChannel.open(outputFile, StandardOpenOption.WRITE)) {
      var length = writeBinaryAndReadHeader(input);
      reader.readPayload(channel, length);
      assertThat(input).isEqualTo(Files.readAllBytes(outputFile));
    } finally {
      Files.delete(outputFile);
    }
  }

  @Property
  void Extension(@ForAll byte[] input, @ForAll byte extensionType) throws IOException {
    packer.packExtensionTypeHeader(extensionType, input.length);
    packer.writePayload(input);
    packer.flush();
    packer.close();
    assertThat(reader.nextType()).isEqualTo(MessageType.EXTENSION);
    var header = reader.readExtensionHeader();
    assertThat(header.type()).isEqualTo(extensionType);
    assertThat(header.length()).isEqualTo(input.length);
    var buffer = ByteBuffer.allocate(header.length());
    reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(input).isEqualTo(output);
  }

  @Property
  void Array(
      @ForAll boolean bool,
      @ForAll byte b,
      @ForAll short s,
      @ForAll int i,
      @ForAll long l,
      @ForAll float f,
      @ForAll double d,
      @ForAll Instant t,
      @ForAll String str)
      throws IOException {
    packer
        .packArrayHeader(10)
        .packNil()
        .packBoolean(bool)
        .packByte(b)
        .packShort(s)
        .packInt(i)
        .packLong(l)
        .packFloat(f)
        .packDouble(d)
        .packTimestamp(t)
        .packString(str)
        .flush();
    packer.close();

    assertThat(reader.nextType()).isEqualTo(MessageType.ARRAY);
    assertThat(reader.readArrayHeader()).isEqualTo(10);
    assertThatNoException().isThrownBy(reader::readNil);
    assertThat(reader.readBoolean()).isEqualTo(bool);
    assertThat(reader.readByte()).isEqualTo(b);
    assertThat(reader.readShort()).isEqualTo(s);
    assertThat(reader.readInt()).isEqualTo(i);
    assertThat(reader.readLong()).isEqualTo(l);
    assertThat(reader.readFloat()).isEqualTo(f);
    assertThat(reader.readDouble()).isEqualTo(d);
    assertThat(reader.readTimestamp()).isEqualTo(t);
    assertThat(reader.readString()).isEqualTo(str);
  }

  @Property
  void StringArray(@ForAll List<String> input) throws IOException {
    packer.packArrayHeader(input.size());
    for (var str : input) {
      packer.packString(str);
    }
    packer.flush();
    packer.close();

    assertThat(reader.readArrayHeader()).isEqualTo(input.size());
    for (var str : input) {
      assertThat(reader.readString()).isEqualTo(str);
    }
  }

  @Property
  void Map(
      @ForAll boolean bool,
      @ForAll byte b,
      @ForAll short s,
      @ForAll int i,
      @ForAll long l,
      @ForAll float f,
      @ForAll double d,
      @ForAll Instant t,
      @ForAll String str)
      throws IOException {
    packer
        .packMapHeader(10)
        .packNil()
        .packBoolean(bool)
        .packBoolean(bool)
        .packByte(b)
        .packByte(b)
        .packShort(s)
        .packShort(s)
        .packInt(i)
        .packInt(i)
        .packLong(l)
        .packLong(l)
        .packFloat(f)
        .packFloat(f)
        .packDouble(d)
        .packDouble(d)
        .packTimestamp(t)
        .packTimestamp(t)
        .packString(str)
        .packString(str)
        .packNil()
        .flush();
    packer.close();

    assertThat(reader.nextType()).isEqualTo(MessageType.MAP);
    assertThat(reader.readMapHeader()).isEqualTo(10);
    assertThatNoException().isThrownBy(reader::readNil);
    assertThat(reader.readBoolean()).isEqualTo(bool);
    assertThat(reader.readBoolean()).isEqualTo(bool);
    assertThat(reader.readByte()).isEqualTo(b);
    assertThat(reader.readByte()).isEqualTo(b);
    assertThat(reader.readShort()).isEqualTo(s);
    assertThat(reader.readShort()).isEqualTo(s);
    assertThat(reader.readInt()).isEqualTo(i);
    assertThat(reader.readInt()).isEqualTo(i);
    assertThat(reader.readLong()).isEqualTo(l);
    assertThat(reader.readLong()).isEqualTo(l);
    assertThat(reader.readFloat()).isEqualTo(f);
    assertThat(reader.readFloat()).isEqualTo(f);
    assertThat(reader.readDouble()).isEqualTo(d);
    assertThat(reader.readDouble()).isEqualTo(d);
    assertThat(reader.readTimestamp()).isEqualTo(t);
    assertThat(reader.readTimestamp()).isEqualTo(t);
    assertThat(reader.readString()).isEqualTo(str);
    assertThat(reader.readString()).isEqualTo(str);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  void StringMap(@ForAll Map<String, String> input) throws IOException {
    packer.packMapHeader(input.size());
    for (var entry : input.entrySet()) {
      packer.packString(entry.getKey()).packString(entry.getValue());
    }
    packer.flush();
    packer.close();

    assertThat(reader.readMapHeader()).isEqualTo(input.size());
    for (var entry : input.entrySet()) {
      assertThat(reader.readString()).isEqualTo(entry.getKey());
      assertThat(reader.readString()).isEqualTo(entry.getValue());
    }
  }

  @Property
  void skipValues(
      @ForAll boolean bool,
      @ForAll byte b,
      @ForAll short s,
      @ForAll int i,
      @ForAll long l,
      @ForAll float f,
      @ForAll double d,
      @ForAll Instant t,
      @ForAll String str)
      throws IOException {
    packer
        .packNil()
        .packBoolean(bool)
        .packByte(b)
        .packShort(s)
        .packInt(i)
        .packLong(l)
        .packFloat(f)
        .packDouble(d)
        .packTimestamp(t)
        .packString(str)
        .packNil()
        .flush();
    packer.close();

    reader.skipValue(10);
    reader.readNil();
  }

  @Property
  void skipStringMap(@ForAll Map<String, String> input) throws IOException {
    packer.packMapHeader(input.size());
    for (var entry : input.entrySet()) {
      packer.packString(entry.getKey()).packString(entry.getValue());
    }
    packer.packNil().flush();
    packer.close();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  void skipStringArray(@ForAll List<String> input) throws IOException {
    packer.packArrayHeader(input.size());
    for (var str : input) {
      packer.packString(str);
    }
    packer.packNil().flush();
    packer.close();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  void skipNested(
      @ForAll @Size(max = 5) List<@Size(max = 3) Map<@StringLength(max = 5) String, Long>> input)
      throws IOException {
    packer.packArrayHeader(input.size());
    for (Map<String, Long> map : input) {
      packer.packMapHeader(map.size());
      for (var entry : map.entrySet()) {
        packer.packString(entry.getKey()).packLong(entry.getValue());
      }
    }
    packer.packNil().flush();
    packer.close();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  void skipBinary(@ForAll byte[] input) throws IOException {
    packer.packBinaryHeader(input.length).writePayload(input).packNil().flush();

    reader.skipValue();
    reader.readNil();
  }

  void writeAndReadString(String input) throws IOException {
    packer.packString(input);
    packer.flush();
    packer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixStr(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.STR8),
            format -> assertThat(format).isEqualTo(MessageFormat.STR16),
            format -> assertThat(format).isEqualTo(MessageFormat.STR32));
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readString();
    assertThat(output).isEqualTo(input);
  }

  private int writeBinaryAndReadHeader(byte[] input) throws IOException {
    return writeBinaryAndReadHeader(input, packer, reader);
  }

  private int writeBinaryAndReadHeader(byte[] input, MessagePacker packer, MessageReader reader)
      throws IOException {
    packer.packBinaryHeader(input.length);
    packer.writePayload(input);
    packer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.BINARY);
    var length = reader.readBinaryHeader();
    assertThat(length).isEqualTo(input.length);
    return length;
  }

  private byte nextFormat() throws IOException {
    return ((MessageReaderImpl) reader).nextFormat();
  }
}
