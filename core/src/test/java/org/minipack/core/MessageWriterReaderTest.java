/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.StringLength;
import org.minipack.core.internal.MessageFormat;

/** Tests {@link MessageReader} against {@link MessageWriter}. */
public abstract sealed class MessageWriterReaderTest {
  private final MessageWriter writer;
  private final MessageReader reader;

  public static final class ChannelToStreamTest extends MessageWriterReaderTest {
    public ChannelToStreamTest() throws IOException {
      super(true);
    }
  }

  public static final class StreamToChannelTest extends MessageWriterReaderTest {
    public StreamToChannelTest() throws IOException {
      super(false);
    }
  }

  public MessageWriterReaderTest(boolean isChannel) throws IOException {
    var in = new PipedInputStream(1 << 16);
    var out = new PipedOutputStream(in);
    var writeAllocator = BufferAllocator.unpooled().build();
    writer =
        MessageWriter.builder()
            .sink(
                isChannel
                    ? MessageSink.of(Channels.newChannel(out), writeAllocator, 1 << 7)
                    : MessageSink.of(out, writeAllocator, 1 << 7))
            .build();
    var readAllocator = BufferAllocator.unpooled().build();
    reader =
        MessageReader.builder()
            .source(
                isChannel
                    ? MessageSource.of(in, readAllocator, 1 << 9)
                    : MessageSource.of(Channels.newChannel(in), readAllocator, 1 << 9))
            .build();
  }

  @Example
  public void writeReadNil() throws IOException {
    writer.writeNil();
    writer.flush();
    assertThat(reader.nextFormat()).isEqualTo(MessageFormat.NIL);
    assertThat(reader.nextType()).isEqualTo(MessageType.NIL);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  public void writeReadBoolean(@ForAll boolean input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat()).isEqualTo(input ? MessageFormat.TRUE : MessageFormat.FALSE);
    assertThat(reader.nextType()).isEqualTo(MessageType.BOOLEAN);
    var output = reader.readBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadByte(@ForAll byte input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
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
  public void writeReadShort(@ForAll short input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
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
  public void writeReadInt(@ForAll int input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
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
  public void writeReadLong(@ForAll long input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
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
  public void writeReadFloat(@ForAll float input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat()).isEqualTo(MessageFormat.FLOAT32);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadDouble(@ForAll double input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat()).isEqualTo(MessageFormat.FLOAT64);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readDouble();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadTimestamp(@ForAll Instant input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.EXTENSION);
    var output = reader.readTimestamp();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadAsciiString(@ForAll @CharRange(to = 127) String input) throws IOException {
    doWriteReadString(input);
  }

  @Property
  public void writeReadString(@ForAll String input) throws IOException {
    doWriteReadString(input);
  }

  @Property
  public void writeReadCharSequence(@ForAll String input) throws IOException {
    doWriteReadString(new StringBuilder(input));
  }

  @Property
  public void writeReadLongAsciiString(
      @ForAll @CharRange(to = 127) @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    doWriteReadString(input);
  }

  @Property
  public void writeReadLongString(@ForAll @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    doWriteReadString(input);
  }

  @Property
  public void writeReadIdentifier(@ForAll @StringLength(max = 1 << 5) String input)
      throws IOException {
    doWriteReadIdentifier(input);
  }

  @Property
  public void writeReadRawString(@ForAll String input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var length = reader.readStringHeader();
    var buffer = ByteBuffer.allocate(length);
    reader.readPayload(buffer);
    var output = new String(buffer.array(), StandardCharsets.UTF_8);
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeBinaryWithByteBufferPayload(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.flush();
    checkBinary(input);
  }

  @Property
  public void writeBinaryWithMultipleByteBuffersPayload1(
      @ForAll byte[] input1, @ForAll byte[] input2) throws IOException {
    var length = input1.length + input2.length;
    writer.writeBinaryHeader(length);
    writer.writePayload(ByteBuffer.wrap(input1));
    writer.writePayload(ByteBuffer.wrap(input2));
    writer.flush();
    var input = ByteBuffer.allocate(length).put(input1).put(input2);
    checkBinary(input.array());
  }

  @Property
  public void writeBinaryWithMultipleByteBuffersPayload2(
      @ForAll byte[] input1, @ForAll byte[] input2) throws IOException {
    var length = input1.length + input2.length;
    writer.writeBinaryHeader(length);
    writer.writePayloads(ByteBuffer.wrap(input1), ByteBuffer.wrap(input2));
    writer.flush();
    var input = ByteBuffer.allocate(length).put(input1).put(input2);
    checkBinary(input.array());
  }

  @Property
  public void writeBinaryWithInputStreamPayload(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    var inputStream = new ByteArrayInputStream(input);
    writer.writePayload(inputStream, Long.MAX_VALUE);
    writer.flush();
    checkBinary(input);
  }

  @Property
  public void writeBinaryWithChannelPayload(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    var inputStream = new ByteArrayInputStream(input);
    var channel = Channels.newChannel(inputStream);
    writer.writePayload(channel, Long.MAX_VALUE);
    writer.flush();
    checkBinary(input);
  }

  @Property(tries = 10)
  public void writeBinaryWithFileChannelPayload(@ForAll byte[] input) throws IOException {
    var inputFile = Files.createTempFile(null, null);
    Files.write(inputFile, input);
    try (var inputStream = new FileInputStream(inputFile.toFile())) {
      writer.writeBinaryHeader(input.length);
      writer.writePayload(inputStream.getChannel(), Long.MAX_VALUE);
      writer.flush();
      checkBinary(input);
    } finally {
      Files.delete(inputFile);
    }
  }

  @Property
  public void writeBinaryWithMixedPayload(
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

  private void checkBinary(byte[] input) throws IOException {
    assertThat(reader.nextType()).isEqualTo(MessageType.BINARY);
    var length = reader.readBinaryHeader();
    var buffer = ByteBuffer.allocate(length);
    reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadExtension(@ForAll byte[] input, @ForAll byte extensionType)
      throws IOException {
    writer.writeExtensionHeader(input.length, extensionType);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.EXTENSION);
    var header = reader.readExtensionHeader();
    assertThat(header.type()).isEqualTo(extensionType);
    var buffer = ByteBuffer.allocate(header.length());
    reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadArray(
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
  public void writeReadStringArray(@ForAll List<String> strings) throws IOException {
    writer.writeArrayHeader(strings.size());
    for (var str : strings) {
      writer.write(str);
    }
    writer.flush();

    assertThat(reader.readArrayHeader()).isEqualTo(strings.size());
    for (var str : strings) {
      assertThat(reader.readString()).isEqualTo(str);
    }
  }

  @Property
  public void writeReadMap(
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
  public void writeReadStringMap(@ForAll Map<String, String> strings) throws IOException {
    writer.writeMapHeader(strings.size());
    for (var entry : strings.entrySet()) {
      writer.write(entry.getKey());
      writer.write(entry.getValue());
    }
    writer.flush();

    assertThat(reader.readMapHeader()).isEqualTo(strings.size());
    for (var entry : strings.entrySet()) {
      assertThat(reader.readString()).isEqualTo(entry.getKey());
      assertThat(reader.readString()).isEqualTo(entry.getValue());
    }
  }

  @Property
  public void writeSkipValues(
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
    writer.flush();

    reader.skipValue(10);
    reader.readNil();
  }

  @Property
  public void writeSkipStringMap(@ForAll Map<String, String> input) throws IOException {
    writer.writeMapHeader(input.size());
    for (var entry : input.entrySet()) {
      writer.write(entry.getKey());
      writer.write(entry.getValue());
    }
    writer.writeNil();
    writer.flush();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  public void writeSkipStringArray(@ForAll List<String> input) throws IOException {
    writer.writeArrayHeader(input.size());
    for (var str : input) {
      writer.write(str);
    }
    writer.writeNil();
    writer.flush();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  public void writeSkipNested(
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
    writer.flush();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  public void writeSkipBinary(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.writeNil();
    writer.flush();

    reader.skipValue();
    reader.readNil();
  }

  private void doWriteReadString(CharSequence input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixStr(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.STR8),
            format -> assertThat(format).isEqualTo(MessageFormat.STR16),
            format -> assertThat(format).isEqualTo(MessageFormat.STR32));
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readString();
    assertThat(output).isEqualTo(input.toString());
  }

  private void doWriteReadIdentifier(String input) throws IOException {
    writer.writeIdentifier(input);
    writer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readIdentifier();
    assertThat(output).isEqualTo(input);
  }
}
