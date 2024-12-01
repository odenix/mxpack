/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

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
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.lifecycle.AfterTry;
import net.jqwik.api.lifecycle.BeforeTry;
import org.msgpack.core.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

/// Tests [MessageWriter] against [org.msgpack.core.MessageUnpacker].
@SuppressWarnings("unused")
abstract sealed class WriteTest {
  private final BufferAllocator allocator;
  private final boolean isChannel;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessageWriter writer;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessageUnpacker unpacker;

  static final class OutputStreamHeapBufferTest extends WriteTest {
    OutputStreamHeapBufferTest() {
      super(false, false);
    }
  }

  static final class ChannelHeapBufferTest extends WriteTest {
    ChannelHeapBufferTest() {
      super(true, false);
    }
  }

  static final class ChannelDirectBufferTest extends WriteTest {
    ChannelDirectBufferTest() {
      super(true, true);
    }
  }

  WriteTest(boolean isChannel, boolean isDirect) {
    this.isChannel = isChannel;
    allocator = isDirect
        ? BufferAllocator.ofPooled(options -> options.preferDirectBuffers(true))
        : BufferAllocator.ofUnpooled();
  }

  @BeforeTry
  void beforeProperty() throws IOException {
    var in = new PipedInputStream(1 << 19);
    var out = new PipedOutputStream(in);
    writer = isChannel
        ? MessageWriter.of(
            Channels.newChannel(out),
            options -> options.allocator(allocator).writeBufferCapacity(1 << 8))
        : MessageWriter.of(out, options -> options.allocator(allocator).writeBufferCapacity(1 << 8));
    unpacker = MessagePack.newDefaultUnpacker(in);
  }

  @AfterTry
  void afterTry() throws IOException {
    unpacker.close();
  }

  void afterProperty() {
    allocator.close();
  }

  @Example
  void Nil() throws IOException {
    writer.writeNil();
    writer.close();
    assertThatNoException().isThrownBy(unpacker::unpackNil);
  }

  @Property
  void Boolean(@ForAll boolean input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat()).isEqualTo(MessageFormat.BOOLEAN);
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.BOOLEAN);
    var output = unpacker.unpackBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Byte(@ForAll byte input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat())
        .satisfiesAnyOf(
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.POSFIXINT : MessageFormat.NEGFIXINT),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT8 : MessageFormat.INT8));
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.INTEGER);
    var output = unpacker.unpackByte();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Short(@ForAll short input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat())
        .satisfiesAnyOf(
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.POSFIXINT : MessageFormat.NEGFIXINT),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? MessageFormat.UINT8 : MessageFormat.INT8),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT16 : MessageFormat.INT16));
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.INTEGER);
    var output = unpacker.unpackShort();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Int(@ForAll int input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat())
        .satisfiesAnyOf(
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.POSFIXINT : MessageFormat.NEGFIXINT),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? MessageFormat.UINT8 : MessageFormat.INT8),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT16 : MessageFormat.INT16),
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.UINT32 : MessageFormat.INT32));
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.INTEGER);
    var output = unpacker.unpackInt();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Long(@ForAll long input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat())
        .satisfiesAnyOf(
            format ->
                assertThat(format)
                    .isEqualTo(input >= 0 ? MessageFormat.POSFIXINT : MessageFormat.NEGFIXINT),
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
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.INTEGER);
    var output = unpacker.unpackLong();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Float(@ForAll float input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.FLOAT);
    var output = unpacker.unpackFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Double(@ForAll double input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.FLOAT);
    var output = unpacker.unpackDouble();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void Timestamp(@ForAll Instant input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.EXTENSION);
    var output = unpacker.unpackTimestamp();
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
  void Identifier(@ForAll @StringLength(max = 1 << 6) String input) throws IOException {
    writer.writeIdentifier(input);
    writer.close();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.STRING);
    var output = unpacker.unpackString();
    assertThat(output).isEqualTo(input);
  }

  @Property
  void RawString(@ForAll String input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.STRING);
    var length = unpacker.unpackRawStringHeader();
    var buffer = ByteBuffer.allocate(length);
    unpacker.readPayload(buffer);
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
        var unpacker = MessagePack.newDefaultUnpacker(Files.newInputStream(outputFile));
        var writer = MessageWriter.of(
            FileChannel.open(outputFile, StandardOpenOption.WRITE),
            options -> options.allocator(allocator).writeBufferCapacity(1 << 8))) {
      writer.writeBinaryHeader(input.length);
      var inputStream = new ByteArrayInputStream(input);
      var channel = Channels.newChannel(inputStream);
      writer.writePayload(channel, Long.MAX_VALUE);
      writer.close();
      checkBinary(input, unpacker);
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
  void BinaryFromDifferentSources(
      @ForAll byte[] input1, @ForAll byte[] input2, @ForAll byte[] input3) throws IOException {
    var length = input1.length + input2.length + input3.length;
    writer.writeBinaryHeader(length);
    writer.writePayload(ByteBuffer.wrap(input1));
    writer.writePayload(new ByteArrayInputStream(input2), Long.MAX_VALUE);
    writer.writePayload(Channels.newChannel(new ByteArrayInputStream(input3)), Long.MAX_VALUE);
    writer.close();
    var input = ByteBuffer.allocate(length).put(input1).put(input2).put(input3);
    checkBinary(input.array());
  }

  @Property
  void Extension(@ForAll byte[] input, @ForAll byte extensionType) throws IOException {
    writer.writeExtensionHeader(input.length, extensionType);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.close();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.EXTENSION);
    var header = unpacker.unpackExtensionTypeHeader();
    assertThat(header.getType()).isEqualTo(extensionType);
    var buffer = ByteBuffer.allocate(header.getLength());
    unpacker.readPayload(buffer);
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

    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.ARRAY);
    assertThat(unpacker.unpackArrayHeader()).isEqualTo(10);
    assertThatNoException().isThrownBy(unpacker::unpackNil);
    assertThat(unpacker.unpackBoolean()).isEqualTo(bool);
    assertThat(unpacker.unpackByte()).isEqualTo(b);
    assertThat(unpacker.unpackShort()).isEqualTo(s);
    assertThat(unpacker.unpackInt()).isEqualTo(i);
    assertThat(unpacker.unpackLong()).isEqualTo(l);
    assertThat(unpacker.unpackFloat()).isEqualTo(f);
    assertThat(unpacker.unpackDouble()).isEqualTo(d);
    assertThat(unpacker.unpackTimestamp()).isEqualTo(t);
    assertThat(unpacker.unpackString()).isEqualTo(str);
  }

  @Property
  void StringArray(@ForAll List<String> input) throws IOException {
    writer.writeArrayHeader(input.size());
    for (var str : input) {
      writer.write(str);
    }
    writer.close();

    assertThat(unpacker.unpackArrayHeader()).isEqualTo(input.size());
    for (var str : input) {
      assertThat(unpacker.unpackString()).isEqualTo(str);
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

    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.MAP);
    assertThat(unpacker.unpackMapHeader()).isEqualTo(10);
    assertThatNoException().isThrownBy(unpacker::unpackNil);
    assertThat(unpacker.unpackBoolean()).isEqualTo(bool);
    assertThat(unpacker.unpackBoolean()).isEqualTo(bool);
    assertThat(unpacker.unpackByte()).isEqualTo(b);
    assertThat(unpacker.unpackByte()).isEqualTo(b);
    assertThat(unpacker.unpackShort()).isEqualTo(s);
    assertThat(unpacker.unpackShort()).isEqualTo(s);
    assertThat(unpacker.unpackInt()).isEqualTo(i);
    assertThat(unpacker.unpackInt()).isEqualTo(i);
    assertThat(unpacker.unpackLong()).isEqualTo(l);
    assertThat(unpacker.unpackLong()).isEqualTo(l);
    assertThat(unpacker.unpackFloat()).isEqualTo(f);
    assertThat(unpacker.unpackFloat()).isEqualTo(f);
    assertThat(unpacker.unpackDouble()).isEqualTo(d);
    assertThat(unpacker.unpackDouble()).isEqualTo(d);
    assertThat(unpacker.unpackTimestamp()).isEqualTo(t);
    assertThat(unpacker.unpackTimestamp()).isEqualTo(t);
    assertThat(unpacker.unpackString()).isEqualTo(str);
    assertThat(unpacker.unpackString()).isEqualTo(str);
    assertThatNoException().isThrownBy(unpacker::unpackNil);
  }

  @Property
  void StringMap(@ForAll Map<String, String> input) throws IOException {
    writer.writeMapHeader(input.size());
    for (var entry : input.entrySet()) {
      writer.write(entry.getKey());
      writer.write(entry.getValue());
    }
    writer.close();

    assertThat(unpacker.unpackMapHeader()).isEqualTo(input.size());
    for (var entry : input.entrySet()) {
      assertThat(unpacker.unpackString()).isEqualTo(entry.getKey());
      assertThat(unpacker.unpackString()).isEqualTo(entry.getValue());
    }
  }

  private void checkBinary(byte[] input) throws IOException {
    checkBinary(input, unpacker);
  }

  private void checkBinary(byte[] input, MessageUnpacker unpacker) throws IOException {
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.BINARY);
    var length = unpacker.unpackBinaryHeader();
    var buffer = ByteBuffer.allocate(length);
    unpacker.readPayload(buffer);
    var output = buffer.array();
    assertThat(output).isEqualTo(input);
  }

  private void writeAndReadString(CharSequence input) throws IOException {
    writer.write(input);
    writer.close();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.STRING);
    var output = unpacker.unpackString();
    assertThat(output).isEqualTo(input.toString());
  }
}
