/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import static org.assertj.core.api.Assertions.*;

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

/// Tests [MessageReader] against [MessageWriter].
@SuppressWarnings("unused")
abstract sealed class RoundtripTest {
  private final BufferAllocator allocator;
  private final boolean isWriterChannel;
  private final boolean isReaderChannel;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessageWriter writer;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessageReader reader;

  static final class StreamToStreamTest extends RoundtripTest {
    StreamToStreamTest() {
      super(false, false, false);
    }
  }

  static final class ChannelToStreamTest extends RoundtripTest {
    ChannelToStreamTest() {
      super(true, false, false);
    }
  }

  static final class StreamToChannelTest extends RoundtripTest {
    StreamToChannelTest() {
      super(false, true, false);
    }
  }

  static final class ChannelToChannelHeapBufferTest extends RoundtripTest {
    ChannelToChannelHeapBufferTest() {
      super(true, true, false);
    }
  }

  static final class ChannelToChannelDirectBufferTest extends RoundtripTest {
    ChannelToChannelDirectBufferTest() {
      super(true, true, true);
    }
  }

  RoundtripTest(
      boolean isWriterChannel, boolean isReaderChannel, boolean isDirect) {
    this.isWriterChannel = isWriterChannel;
    this.isReaderChannel = isReaderChannel;
    allocator = isDirect
        ? BufferAllocator.ofPooled(options -> options.preferDirectBuffers(true))
        : BufferAllocator.ofUnpooled();
  }

  @BeforeTry
  void beforeTry() throws IOException {
    var in = new PipedInputStream(1 << 19);
    var out = new PipedOutputStream(in);
    writer = isWriterChannel
        ? MessageWriter.of(
            Channels.newChannel(out),
            options -> options.allocator(allocator).writeBufferCapacity(1 << 7))
        : MessageWriter.of(out, options -> options.allocator(allocator).writeBufferCapacity(1 << 7));
    reader = isReaderChannel
        ? MessageReader.of(
            Channels.newChannel(in),
            options -> options.allocator(allocator).readBufferCapacity(1 << 9))
        : MessageReader.of(in, options -> options.allocator(allocator).readBufferCapacity(1 << 9));
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
    writer.writeNil();
    writer.close();
    assertThat(nextFormat()).isEqualTo(MessageFormat.NIL);
    assertThat(reader.nextType()).isEqualTo(MessageType.NIL);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  void Boolean(@ForAll boolean input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(nextFormat()).isEqualTo(input ? MessageFormat.TRUE : MessageFormat.FALSE);
    assertThat(reader.nextType()).isEqualTo(MessageType.BOOLEAN);
    var output = reader.readBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Byte(@ForAll byte input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT8 : MessageFormat.INT8));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readByte();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Short(@ForAll short input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? MessageFormat.UINT8 : MessageFormat.INT8),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT16 : MessageFormat.INT16));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readShort();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Int(@ForAll int input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? MessageFormat.UINT8 : MessageFormat.INT8),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT16 : MessageFormat.INT16),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT32 : MessageFormat.INT32));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readInt();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Long(@ForAll long input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? MessageFormat.UINT8 : MessageFormat.INT8),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT16 : MessageFormat.INT16),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT32 : MessageFormat.INT32),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT64 : MessageFormat.INT64));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readLong();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Float(@ForAll float input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(nextFormat()).isEqualTo(MessageFormat.FLOAT32);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Double(@ForAll double input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(nextFormat()).isEqualTo(MessageFormat.FLOAT64);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readDouble();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Timestamp(@ForAll Instant input) throws IOException {
    writer.write(input);
    writer.close();
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
  void CharSequence(@ForAll String input) throws IOException {
    writeAndReadString(new StringBuilder(input));
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
  void Identifier(@ForAll @StringLength(max = 1 << 5) String input)
      throws IOException {
    writer.writeIdentifier(input);
    writer.close();
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readIdentifier();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void RawString(@ForAll String input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var length = reader.readStringHeader();
    var buffer = ByteBuffer.allocate(length);
    reader.readPayload(buffer);
    var output = new String(buffer.array(), StandardCharsets.UTF_8);
    assertThat(output).isEqualTo(input);
  }

  @Property
  void BinaryFromByteBuffer(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.close();
    checkBinary(input);
  }

  @Property
  void BinaryFromMultipleByteBuffers1(@ForAll byte[] input1, @ForAll byte[] input2)
      throws IOException {
    var length = input1.length + input2.length;
    writer.writeBinaryHeader(length);
    writer.writePayload(ByteBuffer.wrap(input1));
    writer.writePayload(ByteBuffer.wrap(input2));
    writer.close();
    var input = ByteBuffer.allocate(length).put(input1).put(input2);
    checkBinary(input.array());
  }

  @Property
  void BinaryFromMultipleByteBuffers2(@ForAll byte[] input1, @ForAll byte[] input2)
      throws IOException {
    var length = input1.length + input2.length;
    writer.writeBinaryHeader(length);
    writer.writePayload(ByteBuffer.wrap(input1), ByteBuffer.wrap(input2));
    writer.close();
    var input = ByteBuffer.allocate(length).put(input1).put(input2);
    checkBinary(input.array());
  }

  @Property
  void BinaryFromInputStream(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    var inputStream = new ByteArrayInputStream(input);
    writer.writePayload(inputStream, Long.MAX_VALUE);
    writer.close();
    checkBinary(input);
  }

  @Property
  void BinaryFromChannel(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    var inputStream = new ByteArrayInputStream(input);
    var channel = Channels.newChannel(inputStream);
    writer.writePayload(channel, Long.MAX_VALUE);
    writer.close();
    checkBinary(input);
  }

  @Property(tries = 10)
  void BinaryFromChannelToFileChannelSink(@ForAll byte[] input) throws IOException {
    var outputFile = Files.createTempFile(null, null);
    try (var allocator = BufferAllocator.ofUnpooled();
         var reader = MessageReader.of(
            Files.newInputStream(outputFile),
            options -> options.allocator(allocator).readBufferCapacity(1 << 9));
         var writer = MessageWriter.of(
            FileChannel.open(outputFile, StandardOpenOption.WRITE),
            options -> options.allocator(allocator).writeBufferCapacity(1 << 7))) {
      writer.writeBinaryHeader(input.length);
      var inputStream = new ByteArrayInputStream(input);
      var channel = Channels.newChannel(inputStream);
      writer.writePayload(channel, Long.MAX_VALUE);
      writer.close();
      checkBinary(input, reader);
    } finally {
      Files.delete(outputFile);
    }
  }

  @Property(tries = 10)
  void BinaryFromFileChannel(@ForAll byte[] input) throws IOException {
    var inputFile = Files.createTempFile(null, null);
    Files.write(inputFile, input);
    try (var channel = FileChannel.open(inputFile)) {
      writer.writeBinaryHeader(input.length);
      writer.writePayload(channel, Long.MAX_VALUE);
      writer.close();
      checkBinary(input);
    } finally {
      Files.delete(inputFile);
    }
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
  void BinaryIntoChannelFromFileChannelSource(@ForAll byte[] input)
      throws IOException {
    var inputFile = Files.createTempFile(null, null);
    try (var allocator = BufferAllocator.ofUnpooled();
         var writer = MessageWriter.of(
            Files.newOutputStream(inputFile),
            options -> options.allocator(allocator).writeBufferCapacity(1 << 7));
         var reader = MessageReader.of(
            FileChannel.open(inputFile),
            options -> options.allocator(allocator).readBufferCapacity(1 << 9))) {
      var length = writeBinaryAndReadHeader(input, writer, reader);
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
  void Extension(@ForAll byte[] input, @ForAll byte extensionType)
      throws IOException {
    writer.writeExtensionHeader(input.length, extensionType);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.close();
    assertThat(reader.nextType()).isEqualTo(MessageType.EXTENSION);
    var header = reader.readExtensionHeader();
    assertThat(header.type()).isEqualTo(extensionType);
    var buffer = ByteBuffer.allocate(header.length());
    reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(output).isEqualTo(input);
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
    writer.writeArrayHeader(10);
    writer.writeNil();
    writer.write(bool);
    writer.write(b);
    writer.write(s);
    writer.write(i);
    writer.write(l);
    writer.write(f);
    writer.write(d);
    writer.write(t);
    writer.write(str);
    writer.close();

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
  void StringArray(@ForAll List<String> strings) throws IOException {
    writer.writeArrayHeader(strings.size());
    for (var str : strings) {
      writer.write(str);
    }
    writer.close();

    assertThat(reader.readArrayHeader()).isEqualTo(strings.size());
    for (var str : strings) {
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
    writer.writeMapHeader(10);
    writer.writeNil();
    writer.write(bool);
    writer.write(bool);
    writer.write(b);
    writer.write(b);
    writer.write(s);
    writer.write(s);
    writer.write(i);
    writer.write(i);
    writer.write(l);
    writer.write(l);
    writer.write(f);
    writer.write(f);
    writer.write(d);
    writer.write(d);
    writer.write(t);
    writer.write(t);
    writer.write(str);
    writer.write(str);
    writer.writeNil();
    writer.close();

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
  void StringMap(@ForAll Map<String, String> strings) throws IOException {
    writer.writeMapHeader(strings.size());
    for (var entry : strings.entrySet()) {
      writer.write(entry.getKey());
      writer.write(entry.getValue());
    }
    writer.close();

    assertThat(reader.readMapHeader()).isEqualTo(strings.size());
    for (var entry : strings.entrySet()) {
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
    writer.writeNil();
    writer.write(bool);
    writer.write(b);
    writer.write(s);
    writer.write(i);
    writer.write(l);
    writer.write(f);
    writer.write(d);
    writer.write(t);
    writer.write(str);
    writer.writeNil();
    writer.close();

    reader.skipValue(10);
    reader.readNil();
  }

  @Property
  void skipStringMap(@ForAll Map<String, String> input) throws IOException {
    writer.writeMapHeader(input.size());
    for (var entry : input.entrySet()) {
      writer.write(entry.getKey());
      writer.write(entry.getValue());
    }
    writer.writeNil();
    writer.close();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  void skipStringArray(@ForAll List<String> input) throws IOException {
    writer.writeArrayHeader(input.size());
    for (var str : input) {
      writer.write(str);
    }
    writer.writeNil();
    writer.close();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  void skipNested(
      @ForAll @Size(max = 5) List<@Size(max = 3) Map<@StringLength(max = 5) String, Long>> input)
      throws IOException {
    writer.writeArrayHeader(input.size());
    for (Map<String, Long> map : input) {
      writer.writeMapHeader(map.size());
      for (var entry : map.entrySet()) {
        writer.write(entry.getKey());
        writer.write(entry.getValue());
      }
    }
    writer.writeNil();
    writer.close();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  void skipBinary(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.writeNil();
    writer.close();

    reader.skipValue();
    reader.readNil();
  }

  private int writeBinaryAndReadHeader(byte[] input) throws IOException {
    return writeBinaryAndReadHeader(input, writer, reader);
  }

  private int writeBinaryAndReadHeader(byte[] input, MessageWriter writer, MessageReader reader)
      throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.close();
    assertThat(reader.nextType()).isEqualTo(MessageType.BINARY);
    var length = reader.readBinaryHeader();
    assertThat(length).isEqualTo(input.length);
    return length;
  }

  private void checkBinary(byte[] input) throws IOException {
    checkBinary(input, reader);
  }

  private void checkBinary(byte[] input, MessageReader reader) throws IOException {
    assertThat(reader.nextType()).isEqualTo(MessageType.BINARY);
    var length = reader.readBinaryHeader();
    var buffer = ByteBuffer.allocate(length);
    reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(output).isEqualTo(input);
  }

  private void writeAndReadString(CharSequence input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixStr(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.STR8),
            format -> assertThat(format).isEqualTo(MessageFormat.STR16),
            format -> assertThat(format).isEqualTo(MessageFormat.STR32));
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readString();
    assertThat(output).isEqualTo(input.toString());
  }

  private byte nextFormat() throws IOException {
    return ((MessageReaderImpl) reader).nextFormat();
  }
}
