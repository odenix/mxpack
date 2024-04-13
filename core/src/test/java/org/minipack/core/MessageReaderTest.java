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
import net.jqwik.api.lifecycle.AfterProperty;
import org.minipack.core.internal.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

/** Tests {@link MessageReader} against {@link org.msgpack.core.MessagePacker}. */
public abstract sealed class MessageReaderTest {
  private final BufferAllocator allocator;
  private final MessagePacker packer;
  private final MessageReader reader;

  public static final class InputStreamHeapBufferTest extends MessageReaderTest {
    public InputStreamHeapBufferTest() throws IOException {
      super(false, false);
    }
  }

  public static final class ChannelHeapBufferTest extends MessageReaderTest {
    public ChannelHeapBufferTest() throws IOException {
      super(true, false);
    }
  }

  public static final class ChannelDirectBufferTest extends MessageReaderTest {
    public ChannelDirectBufferTest() throws IOException {
      super(true, true);
    }
  }

  public MessageReaderTest(boolean isChannel, boolean isDirect) throws IOException {
    var in = new PipedInputStream(1 << 16);
    var out = new PipedOutputStream(in);
    allocator = BufferAllocator.pooled().preferDirect(isDirect).build();
    packer = MessagePack.newDefaultPacker(out);
    reader =
        MessageReader.builder()
            .source(
                isChannel
                    ? MessageSource.of(Channels.newChannel(in), allocator, 1 << 8)
                    : MessageSource.of(in, allocator, 1 << 8))
            .build();
  }

  @AfterProperty
  public void afterProperty() throws IOException {
    packer.close();
    reader.close();
    allocator.close();
  }

  @Example
  public void readNil() throws IOException {
    packer.packNil().flush();
    assertThat(reader.nextFormat()).isEqualTo(MessageFormat.NIL);
    assertThat(reader.nextType()).isEqualTo(MessageType.NIL);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  public void readBoolean(@ForAll boolean input) throws IOException {
    packer.packBoolean(input).flush();
    assertThat(reader.nextFormat()).isEqualTo(input ? MessageFormat.TRUE : MessageFormat.FALSE);
    assertThat(reader.nextType()).isEqualTo(MessageType.BOOLEAN);
    var output = reader.readBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readByte(@ForAll byte input) throws IOException {
    packer.packByte(input).flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.INT8),
            format -> assertThat(format).isEqualTo(MessageFormat.UINT8));
    assertThat(reader.nextType()).isEqualTo(MessageType.INTEGER);
    var output = reader.readByte();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readShort(@ForAll short input) throws IOException {
    packer.packShort(input).flush();
    assertThat(reader.nextFormat())
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
  public void readInt(@ForAll int input) throws IOException {
    packer.packInt(input).flush();
    assertThat(reader.nextFormat())
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
  public void readLong(@ForAll long input) throws IOException {
    packer.packLong(input).flush();
    assertThat(reader.nextFormat())
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
  public void readFloat(@ForAll float input) throws IOException {
    packer.packFloat(input).flush();
    assertThat(reader.nextFormat()).isEqualTo(MessageFormat.FLOAT32);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readDouble(@ForAll double input) throws IOException {
    packer.packDouble(input).flush();
    assertThat(reader.nextFormat()).isEqualTo(MessageFormat.FLOAT64);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readDouble();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readTimestamp(@ForAll Instant input) throws IOException {
    packer.packTimestamp(input).flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.EXTENSION);
    var output = reader.readTimestamp();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readAsciiString(@ForAll @CharRange(to = 127) String input) throws IOException {
    doReadString(input);
  }

  @Property
  public void readString(@ForAll String input) throws IOException {
    doReadString(input);
  }

  @Property
  public void readLongAsciiString(
      @ForAll @CharRange(to = 127) @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    doReadString(input);
  }

  @Property
  public void readLongString(@ForAll @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    doReadString(input);
  }

  @Property
  public void readIdentifier(@ForAll @StringLength(max = 1 << 6) String input) throws IOException {
    doReadIdentifier(input);
  }

  @Property
  public void readRawString(@ForAll String input) throws IOException {
    packer.packString(input);
    packer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var length = reader.readStringHeader();
    var buffer = ByteBuffer.allocate(length);
    reader.readPayload(buffer);
    var output = new String(buffer.array(), StandardCharsets.UTF_8);
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readBinaryIntoByteBuffer(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var buffer = ByteBuffer.allocate(length);
    while (buffer.hasRemaining()) reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(input).isEqualTo(output);
  }

  @Property
  public void readBinaryIntoMultipleByteBuffers1(@ForAll byte[] input1, @ForAll byte[] input2)
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
  public void readBinaryIntoOutputStream(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var outputStream = new ByteArrayOutputStream(length);
    reader.readPayload(outputStream, length);
    var output = outputStream.toByteArray();
    assertThat(input).isEqualTo(output);
  }

  @Property
  public void readBinaryIntoChannel(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var outputStream = new ByteArrayOutputStream(length);
    reader.readPayload(Channels.newChannel(outputStream), length);
    var output = outputStream.toByteArray();
    assertThat(input).isEqualTo(output);
  }

  @Property(tries = 10)
  public void readBinaryIntoChannelFromFileChannelSource(@ForAll byte[] input) throws IOException {
    var inputFile = Files.createTempFile(null, null);
    var allocator = BufferAllocator.unpooled().build();
    try (var packer = MessagePack.newDefaultPacker(Files.newOutputStream(inputFile));
        var inputStream = new FileInputStream(inputFile.toFile());
        var source = MessageSource.of(inputStream.getChannel(), allocator, 1 << 8);
        var reader = MessageReader.builder().source(source).build()) {
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
  public void readBinaryIntoFileChannel(@ForAll byte[] input) throws IOException {
    var outputFile = Files.createTempFile(null, null);
    try (var outputStream = new FileOutputStream(outputFile.toFile())) {
      var length = writeBinaryAndReadHeader(input);
      reader.readPayload(outputStream.getChannel(), length);
      assertThat(input).isEqualTo(Files.readAllBytes(outputFile));
    } finally {
      Files.delete(outputFile);
    }
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

  @Property
  public void readExtension(@ForAll byte[] input, @ForAll byte extensionType) throws IOException {
    packer.packExtensionTypeHeader(extensionType, input.length);
    packer.writePayload(input);
    packer.flush();
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
  public void readArray(
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
  public void readStringArray(@ForAll List<String> input) throws IOException {
    packer.packArrayHeader(input.size());
    for (var str : input) {
      packer.packString(str);
    }
    packer.flush();

    assertThat(reader.readArrayHeader()).isEqualTo(input.size());
    for (var str : input) {
      assertThat(reader.readString()).isEqualTo(str);
    }
  }

  @Property
  public void readMap(
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
  public void readStringMap(@ForAll Map<String, String> input) throws IOException {
    packer.packMapHeader(input.size());
    for (var entry : input.entrySet()) {
      packer.packString(entry.getKey()).packString(entry.getValue());
    }
    packer.flush();

    assertThat(reader.readMapHeader()).isEqualTo(input.size());
    for (var entry : input.entrySet()) {
      assertThat(reader.readString()).isEqualTo(entry.getKey());
      assertThat(reader.readString()).isEqualTo(entry.getValue());
    }
  }

  @Property
  public void skipValues(
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

    reader.skipValue(10);
    reader.readNil();
  }

  @Property
  public void skipStringMap(@ForAll Map<String, String> input) throws IOException {
    packer.packMapHeader(input.size());
    for (var entry : input.entrySet()) {
      packer.packString(entry.getKey()).packString(entry.getValue());
    }
    packer.packNil().flush();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  public void skipStringArray(@ForAll List<String> input) throws IOException {
    packer.packArrayHeader(input.size());
    for (var str : input) {
      packer.packString(str);
    }
    packer.packNil().flush();

    reader.skipValue();
    reader.readNil();
  }

  @Property
  public void skipNested(
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

    reader.skipValue();
    reader.readNil();
  }

  @Property
  public void skipBinary(@ForAll byte[] input) throws IOException {
    packer.packBinaryHeader(input.length).writePayload(input).packNil().flush();

    reader.skipValue();
    reader.readNil();
  }

  private void doReadString(String input) throws IOException {
    packer.packString(input);
    packer.flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixStr(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.STR8),
            format -> assertThat(format).isEqualTo(MessageFormat.STR16),
            format -> assertThat(format).isEqualTo(MessageFormat.STR32));
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readString();
    assertThat(output).isEqualTo(input);
  }

  private void doReadIdentifier(String input) throws IOException {
    packer.packString(input);
    packer.flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(MessageFormat.isFixStr(format)).isTrue(),
            format -> assertThat(format).isEqualTo(MessageFormat.STR8),
            format -> assertThat(format).isEqualTo(MessageFormat.STR16),
            format -> assertThat(format).isEqualTo(MessageFormat.STR32));
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readIdentifier();
    assertThat(output).isEqualTo(input);
  }
}
