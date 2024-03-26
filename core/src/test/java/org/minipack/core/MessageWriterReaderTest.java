/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.StringLength;
import org.minipack.core.internal.ValueFormat;

/** Tests {@link MessageReader} against {@link MessageWriter}. */
public class MessageWriterReaderTest {
  private final MessageWriter<CharSequence, String> writer;
  private final MessageReader<String, String> reader;

  public MessageWriterReaderTest() throws IOException {
    var in = new PipedInputStream(1 << 16);
    var out = new PipedOutputStream(in);
    writer = MessageWriter.builder().sink(out).buffer(ByteBuffer.allocate(1 << 7)).build();
    reader = MessageReader.builder().source(in).buffer(ByteBuffer.allocate(1 << 9)).build();
  }

  @Example
  public void writeReadNil() throws IOException {
    writer.writeNil();
    writer.flush();
    assertThat(reader.nextFormat()).isEqualTo(ValueFormat.NIL);
    assertThat(reader.nextType()).isEqualTo(ValueType.NIL);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  public void writeReadBoolean(@ForAll boolean input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat()).isEqualTo(input ? ValueFormat.TRUE : ValueFormat.FALSE);
    assertThat(reader.nextType()).isEqualTo(ValueType.BOOLEAN);
    var output = reader.readBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadByte(@ForAll byte input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixInt(format)).isTrue(),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT8 : ValueFormat.INT8));
    assertThat(reader.nextType()).isEqualTo(ValueType.INTEGER);
    var output = reader.readByte();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadShort(@ForAll short input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixInt(format)).isTrue(),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT8 : ValueFormat.INT8),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT16 : ValueFormat.INT16));
    assertThat(reader.nextType()).isEqualTo(ValueType.INTEGER);
    var output = reader.readShort();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadInt(@ForAll int input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixInt(format)).isTrue(),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT8 : ValueFormat.INT8),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT16 : ValueFormat.INT16),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT32 : ValueFormat.INT32));
    assertThat(reader.nextType()).isEqualTo(ValueType.INTEGER);
    var output = reader.readInt();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadLong(@ForAll long input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixInt(format)).isTrue(),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT8 : ValueFormat.INT8),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT16 : ValueFormat.INT16),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT32 : ValueFormat.INT32),
            format ->
                assertThat(format).isEqualTo(input >= 0 ? ValueFormat.UINT64 : ValueFormat.INT64));
    assertThat(reader.nextType()).isEqualTo(ValueType.INTEGER);
    var output = reader.readLong();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadFloat(@ForAll float input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat()).isEqualTo(ValueFormat.FLOAT32);
    assertThat(reader.nextType()).isEqualTo(ValueType.FLOAT);
    var output = reader.readFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void writeReadDouble(@ForAll double input) throws IOException {
    writer.write(input);
    writer.flush();
    assertThat(reader.nextFormat()).isEqualTo(ValueFormat.FLOAT64);
    assertThat(reader.nextType()).isEqualTo(ValueType.FLOAT);
    var output = reader.readDouble();
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
  public void writeReadArray(
      @ForAll boolean bool,
      @ForAll byte b,
      @ForAll short s,
      @ForAll int i,
      @ForAll long l,
      @ForAll float f,
      @ForAll double d,
      @ForAll String str)
      throws IOException {
    writer.writeArrayHeader(9);
    writer.writeNil();
    writer.write(bool);
    writer.write(b);
    writer.write(s);
    writer.write(i);
    writer.write(l);
    writer.write(f);
    writer.write(d);
    writer.writeString(str);
    writer.flush();

    assertThat(reader.nextType()).isEqualTo(ValueType.ARRAY);
    assertThat(reader.readArrayHeader()).isEqualTo(9);
    assertThatNoException().isThrownBy(reader::readNil);
    assertThat(reader.readBoolean()).isEqualTo(bool);
    assertThat(reader.readByte()).isEqualTo(b);
    assertThat(reader.readShort()).isEqualTo(s);
    assertThat(reader.readInt()).isEqualTo(i);
    assertThat(reader.readLong()).isEqualTo(l);
    assertThat(reader.readFloat()).isEqualTo(f);
    assertThat(reader.readDouble()).isEqualTo(d);
    assertThat(reader.readString()).isEqualTo(str);
  }

  @Property
  public void writeReadStringArray(@ForAll List<String> strings) throws IOException {
    writer.writeArrayHeader(strings.size());
    for (var str : strings) {
      writer.writeString(str);
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
      @ForAll String str)
      throws IOException {
    writer.writeMapHeader(9);
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
    writer.writeString(str);
    writer.writeString(str);
    writer.writeNil();
    writer.flush();

    assertThat(reader.nextType()).isEqualTo(ValueType.MAP);
    assertThat(reader.readMapHeader()).isEqualTo(9);
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
    assertThat(reader.readString()).isEqualTo(str);
    assertThat(reader.readString()).isEqualTo(str);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  public void writeReadStringMap(@ForAll Map<String, String> strings) throws IOException {
    writer.writeMapHeader(strings.size());
    for (var entry : strings.entrySet()) {
      writer.writeString(entry.getKey());
      writer.writeString(entry.getValue());
    }
    writer.flush();

    assertThat(reader.readMapHeader()).isEqualTo(strings.size());
    for (var entry : strings.entrySet()) {
      assertThat(reader.readString()).isEqualTo(entry.getKey());
      assertThat(reader.readString()).isEqualTo(entry.getValue());
    }
  }

  private void doWriteReadString(String input) throws IOException {
    writer.writeString(input);
    writer.flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixStr(format)).isTrue(),
            format -> assertThat(format).isEqualTo(ValueFormat.STR8),
            format -> assertThat(format).isEqualTo(ValueFormat.STR16),
            format -> assertThat(format).isEqualTo(ValueFormat.STR32));
    assertThat(reader.nextType()).isEqualTo(ValueType.STRING);
    var output = reader.readString();
    assertThat(output).isEqualTo(input);
  }
}
