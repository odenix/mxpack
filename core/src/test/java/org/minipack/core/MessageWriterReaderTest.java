/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.StringLength;
import org.minipack.core.internal.MessageFormat;

/** Tests {@link MessageReader} against {@link MessageWriter}. */
public abstract class MessageWriterReaderTest {
  private final MessageWriter writer;
  private final MessageReader reader;

  public static class ChannelToStreamTest extends MessageWriterReaderTest {
    public ChannelToStreamTest() throws IOException {
      super(true);
    }
  }

  public static class StreamToChannelTest extends MessageWriterReaderTest {
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
  public void readRawString(@ForAll String input) throws IOException {
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
  public void readRawBinary(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.BINARY);
    var length = reader.readBinaryHeader();
    var buffer = ByteBuffer.allocate(length);
    reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readExtension(@ForAll byte[] input, @ForAll byte extensionType) throws IOException {
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
