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
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.api.lifecycle.AfterTry;
import net.jqwik.api.lifecycle.BeforeTry;
import org.minipack.java.internal.DefaultMessageReader;
import org.minipack.java.internal.MessageFormat;

/// Tests [MessageReader] against [MessageWriter].
public abstract sealed class MessageWriterReaderTest {
  private final BufferAllocator allocator;
  private final boolean isWriterChannel;
  private final boolean isReaderChannel;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessageWriter writer;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessageReader reader;

  public static final class StreamToStreamTest extends MessageWriterReaderTest {
    public StreamToStreamTest() {
      super(false, false, false);
    }
  }

  public static final class ChannelToStreamTest extends MessageWriterReaderTest {
    public ChannelToStreamTest() {
      super(true, false, false);
    }
  }

  public static final class StreamToChannelTest extends MessageWriterReaderTest {
    public StreamToChannelTest() {
      super(false, true, false);
    }
  }

  public static final class ChannelToChannelHeapBufferTest extends MessageWriterReaderTest {
    public ChannelToChannelHeapBufferTest() {
      super(true, true, false);
    }
  }

  public static final class ChannelToChannelDirectBufferTest extends MessageWriterReaderTest {
    public ChannelToChannelDirectBufferTest() {
      super(true, true, true);
    }
  }

  public MessageWriterReaderTest(
      boolean isWriterChannel, boolean isReaderChannel, boolean isDirect) {
    this.isWriterChannel = isWriterChannel;
    this.isReaderChannel = isReaderChannel;
    allocator = BufferAllocator.ofUnpooled(options -> options.useDirectBuffers(isDirect));
  }

  @BeforeTry
  public void beforeTry() throws IOException {
    var in = new PipedInputStream(1 << 19);
    var out = new PipedOutputStream(in);
    var sink =
        isWriterChannel
            ? MessageSink.of(
                Channels.newChannel(out),
                options -> options.allocator(allocator).bufferCapacity(1 << 7))
            : MessageSink.of(out, options -> options.allocator(allocator).bufferCapacity(1 << 7));
    writer = MessageWriter.of(sink);
    var source =
        isReaderChannel
            ? MessageSource.of(
                Channels.newChannel(in),
                options -> options.allocator(allocator).bufferCapacity(1 << 9))
            : MessageSource.of(in, options -> options.allocator(allocator).bufferCapacity(1 << 9));
    reader = MessageReader.of(source);
  }

  @AfterTry
  public void afterTry() throws IOException {
    writer.close();
    reader.close();
  }

  @AfterProperty
  public void afterProperty() {
    allocator.close();
  }

  @Example
  public void roundtripNil() throws IOException {
    writer.writeNil();
    writer.flush();
    assertThat(nextFormat()).isEqualTo(MessageFormat.NIL);
    assertThat(reader.nextType()).isEqualTo(MessageType.NIL);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  public void roundtripBoolean(@ForAll boolean input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(nextFormat()).isEqualTo(input ? MessageFormat.TRUE : MessageFormat.FALSE);
    assertThat(reader.nextType()).isEqualTo(MessageType.BOOLEAN);
    var output = reader.readBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void roundtripByte(@ForAll byte input) throws IOException {
    writer.write(input);
    writer.flush();
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
  public void roundtripShort(@ForAll short input) throws IOException {
    writer.write(input);
    writer.flush();
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
  public void roundtripInt(@ForAll int input) throws IOException {
    writer.write(input);
    writer.flush();
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
  public void roundtripLong(@ForAll long input) throws IOException {
    writer.write(input);
    writer.flush();
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
  public void roundtripFloat(@ForAll float input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(nextFormat()).isEqualTo(MessageFormat.FLOAT32);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void roundtripDouble(@ForAll double input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(nextFormat()).isEqualTo(MessageFormat.FLOAT64);
    assertThat(reader.nextType()).isEqualTo(MessageType.FLOAT);
    var output = reader.readDouble();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void roundtripTimestamp(@ForAll Instant input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.EXTENSION);
    var output = reader.readTimestamp();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void roundtripAsciiString(@ForAll @CharRange(to = 127) String input) throws IOException {
    doRoundtripString(input);
  }

  @Property
  public void roundtripString(@ForAll String input) throws IOException {
    doRoundtripString(input);
  }

  @Property
  public void roundtripCharSequence(@ForAll String input) throws IOException {
    doRoundtripString(new StringBuilder(input));
  }

  @Property
  public void roundtripLongAsciiString(
      @ForAll @CharRange(to = 127) @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    doRoundtripString(input);
  }

  @Property
  public void roundtripLongString(@ForAll @StringLength(min = 1 << 5, max = 1 << 10) String input)
      throws IOException {
    doRoundtripString(input);
  }

  @Property
  public void roundtripIdentifier(@ForAll @StringLength(max = 1 << 5) String input)
      throws IOException {
    doRoundtripIdentifier(input);
  }

  @Property
  public void roundtripRawString(@ForAll String input) throws IOException {
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
  public void roundtripBinaryFromByteBuffer(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.flush();
    checkBinary(input);
  }

  @Property
  public void roundtripBinaryFromMultipleByteBuffers1(@ForAll byte[] input1, @ForAll byte[] input2)
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
  public void roundtripBinaryFromMultipleByteBuffers2(@ForAll byte[] input1, @ForAll byte[] input2)
      throws IOException {
    var length = input1.length + input2.length;
    writer.writeBinaryHeader(length);
    writer.writePayloads(ByteBuffer.wrap(input1), ByteBuffer.wrap(input2));
    writer.flush();
    var input = ByteBuffer.allocate(length).put(input1).put(input2);
    checkBinary(input.array());
  }

  @Property
  public void roundtripBinaryFromInputStream(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    var inputStream = new ByteArrayInputStream(input);
    writer.writePayload(inputStream, Long.MAX_VALUE);
    writer.flush();
    checkBinary(input);
  }

  @Property
  public void roundtripBinaryFromChannel(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    var inputStream = new ByteArrayInputStream(input);
    var channel = Channels.newChannel(inputStream);
    writer.writePayload(channel, Long.MAX_VALUE);
    writer.flush();
    checkBinary(input);
  }

  @Property(tries = 10)
  public void roundtripBinaryFromChannelToFileChannelSink(@ForAll byte[] input) throws IOException {
    var outputFile = Files.createTempFile(null, null);
    try (var allocator = BufferAllocator.ofUnpooled();
        var source =
            MessageSource.of(
                Files.newInputStream(outputFile),
                options -> options.allocator(allocator).bufferCapacity(1 << 9));
        var reader = MessageReader.of(source);
        var sink =
            MessageSink.of(
                FileChannel.open(outputFile, StandardOpenOption.WRITE),
                options -> options.allocator(allocator).bufferCapacity(1 << 7));
        var writer = MessageWriter.of(sink)) {
      writer.writeBinaryHeader(input.length);
      var inputStream = new ByteArrayInputStream(input);
      var channel = Channels.newChannel(inputStream);
      writer.writePayload(channel, Long.MAX_VALUE);
      writer.flush();
      checkBinary(input, reader);
    } finally {
      Files.delete(outputFile);
    }
  }

  @Property(tries = 10)
  public void roundtripBinaryFromFileChannel(@ForAll byte[] input) throws IOException {
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
  public void roundtripBinaryIntoByteBuffer(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var buffer = ByteBuffer.allocate(length);
    while (buffer.hasRemaining()) reader.readPayload(buffer);
    var output = buffer.array();
    assertThat(input).isEqualTo(output);
  }

  @Property
  public void roundtripBinaryIntoMultipleByteBuffers1(@ForAll byte[] input1, @ForAll byte[] input2)
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
  public void roundtripBinaryIntoOutputStream(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var outputStream = new ByteArrayOutputStream(length);
    reader.readPayload(outputStream, length);
    var output = outputStream.toByteArray();
    assertThat(input).isEqualTo(output);
  }

  @Property
  public void roundtripBinaryIntoChannel(@ForAll byte[] input) throws IOException {
    var length = writeBinaryAndReadHeader(input);
    var outputStream = new ByteArrayOutputStream(length);
    reader.readPayload(Channels.newChannel(outputStream), length);
    var output = outputStream.toByteArray();
    assertThat(input).isEqualTo(output);
  }

  @Property(tries = 10)
  public void roundtripBinaryIntoChannelFromFileChannelSource(@ForAll byte[] input)
      throws IOException {
    var inputFile = Files.createTempFile(null, null);
    try (var allocator = BufferAllocator.ofUnpooled();
        var sink =
            MessageSink.of(
                Files.newOutputStream(inputFile),
                options -> options.allocator(allocator).bufferCapacity(1 << 7));
        var writer = MessageWriter.of(sink);
        var source =
            MessageSource.of(
                FileChannel.open(inputFile),
                options -> options.allocator(allocator).bufferCapacity(1 << 9));
        var reader = MessageReader.of(source)) {
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
  public void roundtripBinaryIntoFileChannel(@ForAll byte[] input) throws IOException {
    var outputFile = Files.createTempFile(null, null);
    try (var channel = FileChannel.open(outputFile, StandardOpenOption.WRITE)) {
      var length = writeBinaryAndReadHeader(input);
      reader.readPayload(channel, length);
      assertThat(input).isEqualTo(Files.readAllBytes(outputFile));
    } finally {
      Files.delete(outputFile);
    }
  }

  private int writeBinaryAndReadHeader(byte[] input) throws IOException {
    return writeBinaryAndReadHeader(input, writer, reader);
  }

  private int writeBinaryAndReadHeader(byte[] input, MessageWriter writer, MessageReader reader)
      throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.BINARY);
    var length = reader.readBinaryHeader();
    assertThat(length).isEqualTo(input.length);
    return length;
  }

  @Property
  public void roundtripExtension(@ForAll byte[] input, @ForAll byte extensionType)
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
  public void roundtripArray(
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
  public void roundtripStringArray(@ForAll List<String> strings) throws IOException {
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
  public void roundtripMap(
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
  public void roundtripStringMap(@ForAll Map<String, String> strings) throws IOException {
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
  public void skipStringMap(@ForAll Map<String, String> input) throws IOException {
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
  public void skipStringArray(@ForAll List<String> input) throws IOException {
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
  public void skipNested(
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
  public void skipBinary(@ForAll byte[] input) throws IOException {
    writer.writeBinaryHeader(input.length);
    writer.writePayload(ByteBuffer.wrap(input));
    writer.writeNil();
    writer.flush();

    reader.skipValue();
    reader.readNil();
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

  private void doRoundtripString(CharSequence input) throws IOException {
    writer.write(input);
    writer.flush();
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

  private void doRoundtripIdentifier(String input) throws IOException {
    writer.writeIdentifier(input);
    writer.flush();
    assertThat(reader.nextType()).isEqualTo(MessageType.STRING);
    var output = reader.readIdentifier();
    assertThat(output).isEqualTo(input);
  }

  private byte nextFormat() throws IOException {
    return ((DefaultMessageReader) reader).nextFormat();
  }
}
