/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java;

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
import net.jqwik.api.lifecycle.AfterProperty;
import org.msgpack.core.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

/** Tests {@link MessageWriter} against {@link org.msgpack.core.MessageUnpacker}. */
public abstract sealed class MessageWriterTest {
  private final BufferAllocator allocator;
  private final MessageWriter writer;
  private final MessageUnpacker unpacker;

  public static final class OutputStreamHeapBufferTest extends MessageWriterTest {
    public OutputStreamHeapBufferTest() throws IOException {
      super(false, false);
    }
  }

  public static final class ChannelHeapBufferTest extends MessageWriterTest {
    public ChannelHeapBufferTest() throws IOException {
      super(true, false);
    }
  }

  public static final class ChannelDirectBufferTest extends MessageWriterTest {
    public ChannelDirectBufferTest() throws IOException {
      super(true, true);
    }
  }

  public MessageWriterTest(boolean isChannel, boolean isDirect) throws IOException {
    var in = new PipedInputStream(1 << 16);
    var out = new PipedOutputStream(in);
    allocator = BufferAllocator.ofUnpooled(options -> options.directBuffers(isDirect));
    var sink =
        isChannel
            ? MessageSink.of(
                Channels.newChannel(out),
                options -> options.allocator(allocator).bufferCapacity(1 << 8))
            : MessageSink.of(out, options -> options.allocator(allocator).bufferCapacity(1 << 8));
    writer = MessageWriter.of(sink);
    unpacker = MessagePack.newDefaultUnpacker(in);
  }

  @AfterProperty
  public void afterProperty() throws IOException {
    writer.close();
    unpacker.close();
    allocator.close();
  }

  @Example
  public void writeNil() throws IOException {
    writer.writeNil();
    writer.flush();
    assertThatNoException().isThrownBy(unpacker::unpackNil);
  }

  @Property
  public void writeBoolean(@ForAll boolean input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(unpacker.getNextFormat()).isEqualTo(MessageFormat.BOOLEAN);
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.BOOLEAN);
    var output = unpacker.unpackBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeByte(@ForAll byte input) throws IOException {
    writer.write(input);
    writer.flush();
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
  public void writeShort(@ForAll short input) throws IOException {
    writer.write(input);
    writer.flush();
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
  public void writeInt(@ForAll int input) throws IOException {
    writer.write(input);
    writer.flush();
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
  public void writeLong(@ForAll long input) throws IOException {
    writer.write(input);
    writer.flush();
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
  public void writeFloat(@ForAll float input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.FLOAT);
    var output = unpacker.unpackFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeDouble(@ForAll double input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.FLOAT);
    var output = unpacker.unpackDouble();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeTimestamp(@ForAll Instant input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.EXTENSION);
    var output = unpacker.unpackTimestamp();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeAsciiString(@ForAll @CharRange(to = 127) String input) throws IOException {
    doWriteString(input);
  }

  @Property
  public void writeString(@ForAll String input) throws IOException {
    doWriteString(input);
  }

  @Property
  public void writeCharSequence(@ForAll String input) throws IOException {
    doWriteString(new StringBuilder(input));
  }

  @Property
  public void writeLongAsciiString(
      @ForAll @CharRange(to = 127) @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    doWriteString(input);
  }

  @Property
  public void writeLongString(@ForAll @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    doWriteString(input);
  }

  @Property
  public void writeIdentifier(@ForAll @StringLength(max = 1 << 6) String input) throws IOException {
    doWriteIdentifier(input);
  }

  @Property
  public void writeRawString(@ForAll String input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.STRING);
    var length = unpacker.unpackRawStringHeader();
    var buffer = ByteBuffer.allocate(length);
    unpacker.readPayload(buffer);
    var output = new String(buffer.array(), StandardCharsets.UTF_8);
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeBinaryFromByteBuffer(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.flush();
    checkBinary(input);
  }

  @Property
  public void writeBinaryFromMultipleByteBuffers1(@ForAll byte[] input1, @ForAll byte[] input2)
      throws IOException {
    var length = input1.length + input2.length;
    writer.writeBinaryHeader(length);
    writer.writePayload(ByteBuffer.wrap(input1));
    writer.writePayload(ByteBuffer.wrap(input2));
    writer.flush();
    var input = ByteBuffer.allocate(length).put(input1).put(input2);
    checkBinary(input.array());
  }

  @Property
  public void writeBinaryFromMultipleByteBuffers2(@ForAll byte[] input1, @ForAll byte[] input2)
      throws IOException {
    var length = input1.length + input2.length;
    writer.writeBinaryHeader(length);
    writer.writePayloads(ByteBuffer.wrap(input1), ByteBuffer.wrap(input2));
    writer.flush();
    var input = ByteBuffer.allocate(length).put(input1).put(input2);
    checkBinary(input.array());
  }

  @Property
  public void writeBinaryFromInputStream(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    var inputStream = new ByteArrayInputStream(input);
    writer.writePayload(inputStream, Long.MAX_VALUE);
    writer.flush();
    checkBinary(input);
  }

  @Property
  public void writeBinaryFromChannel(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    var inputStream = new ByteArrayInputStream(input);
    var channel = Channels.newChannel(inputStream);
    writer.writePayload(channel, Long.MAX_VALUE);
    writer.flush();
    checkBinary(input);
  }

  @Property(tries = 10)
  public void writeBinaryFromChannelToFileChannelSink(@ForAll byte[] input) throws IOException {
    var outputFile = Files.createTempFile(null, null);
    try (var allocator = BufferAllocator.ofUnpooled();
        var unpacker = MessagePack.newDefaultUnpacker(Files.newInputStream(outputFile));
        var sink =
            MessageSink.of(
                FileChannel.open(outputFile, StandardOpenOption.WRITE),
                options -> options.allocator(allocator).bufferCapacity(1 << 8));
        var writer = MessageWriter.of(sink)) {
      writer.writeBinaryHeader(input.length);
      var inputStream = new ByteArrayInputStream(input);
      var channel = Channels.newChannel(inputStream);
      writer.writePayload(channel, Long.MAX_VALUE);
      writer.flush();
      checkBinary(input, unpacker);
    } finally {
      Files.delete(outputFile);
    }
  }

  @Property(tries = 10)
  public void writeBinaryFromFileChannel(@ForAll byte[] input) throws IOException {
    var inputFile = Files.createTempFile(null, null);
    Files.write(inputFile, input);
    try (var channel = FileChannel.open(inputFile)) {
      writer.writeBinaryHeader(input.length);
      writer.writePayload(channel, Long.MAX_VALUE);
      writer.flush();
      checkBinary(input);
    } finally {
      Files.delete(inputFile);
    }
  }

  @Property
  public void writeBinaryFromDifferentSources(
      @ForAll byte[] input1, @ForAll byte[] input2, @ForAll byte[] input3) throws IOException {
    var length = input1.length + input2.length + input3.length;
    writer.writeBinaryHeader(length);
    writer.writePayload(ByteBuffer.wrap(input1));
    writer.writePayload(new ByteArrayInputStream(input2), Long.MAX_VALUE);
    writer.writePayload(Channels.newChannel(new ByteArrayInputStream(input3)), Long.MAX_VALUE);
    writer.flush();
    var input = ByteBuffer.allocate(length).put(input1).put(input2).put(input3);
    checkBinary(input.array());
  }

  @Property
  public void writeExtension(@ForAll byte[] input, @ForAll byte extensionType) throws IOException {
    writer.writeExtensionHeader(input.length, extensionType);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.flush();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.EXTENSION);
    var header = unpacker.unpackExtensionTypeHeader();
    assertThat(header.getType()).isEqualTo(extensionType);
    var buffer = ByteBuffer.allocate(header.getLength());
    unpacker.readPayload(buffer);
    var output = buffer.array();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeArray(
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
    writer.flush();

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
  public void writeStringArray(@ForAll List<String> input) throws IOException {
    writer.writeArrayHeader(input.size());
    for (var str : input) {
      writer.write(str);
    }
    writer.flush();

    assertThat(unpacker.unpackArrayHeader()).isEqualTo(input.size());
    for (var str : input) {
      assertThat(unpacker.unpackString()).isEqualTo(str);
    }
  }

  @Property
  public void writeMap(
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
    writer.flush();

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
  public void writeStringMap(@ForAll Map<String, String> input) throws IOException {
    writer.writeMapHeader(input.size());
    for (var entry : input.entrySet()) {
      writer.write(entry.getKey());
      writer.write(entry.getValue());
    }
    writer.flush();

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

  private void doWriteString(CharSequence input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.STRING);
    var output = unpacker.unpackString();
    assertThat(output).isEqualTo(input.toString());
  }

  private void doWriteIdentifier(String input) throws IOException {
    writer.writeIdentifier(input);
    writer.flush();
    assertThat(unpacker.getNextFormat().getValueType()).isEqualTo(ValueType.STRING);
    var output = unpacker.unpackString();
    assertThat(output).isEqualTo(input);
  }
}