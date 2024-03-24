/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

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
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.translatenix.minipack.internal.ValueFormat;

/** Tests {@link MessageReader} against {@link org.msgpack.core.MessagePacker}. */
public class MessageReaderTest {
  private final MessagePacker packer;
  private final MessageReader<String> reader;

  public MessageReaderTest() throws IOException {
    var in = new PipedInputStream(1 << 16);
    var out = new PipedOutputStream(in);
    packer = MessagePack.newDefaultPacker(out);
    reader = MessageReader.builder().source(in).buffer(ByteBuffer.allocate(1 << 8)).build();
  }

  @Example
  public void readNil() throws IOException {
    packer.packNil().flush();
    assertThat(reader.nextFormat()).isEqualTo(ValueFormat.NIL);
    assertThat(reader.nextType()).isEqualTo(ValueType.NIL);
    assertThatNoException().isThrownBy(reader::readNil);
  }

  @Property
  public void readBoolean(@ForAll boolean input) throws IOException {
    packer.packBoolean(input).flush();
    assertThat(reader.nextFormat()).isEqualTo(input ? ValueFormat.TRUE : ValueFormat.FALSE);
    assertThat(reader.nextType()).isEqualTo(ValueType.BOOLEAN);
    var output = reader.readBoolean();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readByte(@ForAll byte input) throws IOException {
    packer.packByte(input).flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(ValueFormat.INT8),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT8));
    assertThat(reader.nextType()).isEqualTo(ValueType.INTEGER);
    var output = reader.readByte();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readShort(@ForAll short input) throws IOException {
    packer.packShort(input).flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(ValueFormat.INT8),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT8),
            format -> assertThat(format).isEqualTo(ValueFormat.INT16),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT16));
    assertThat(reader.nextType()).isEqualTo(ValueType.INTEGER);
    var output = reader.readShort();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readInt(@ForAll int input) throws IOException {
    packer.packInt(input).flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(ValueFormat.INT8),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT8),
            format -> assertThat(format).isEqualTo(ValueFormat.INT16),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT16),
            format -> assertThat(format).isEqualTo(ValueFormat.INT32),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT32));
    assertThat(reader.nextType()).isEqualTo(ValueType.INTEGER);
    var output = reader.readInt();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readLong(@ForAll long input) throws IOException {
    packer.packLong(input).flush();
    assertThat(reader.nextFormat())
        .satisfiesAnyOf(
            format -> assertThat(ValueFormat.isFixInt(format)).isTrue(),
            format -> assertThat(format).isEqualTo(ValueFormat.INT8),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT8),
            format -> assertThat(format).isEqualTo(ValueFormat.INT16),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT16),
            format -> assertThat(format).isEqualTo(ValueFormat.INT32),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT32),
            format -> assertThat(format).isEqualTo(ValueFormat.INT64),
            format -> assertThat(format).isEqualTo(ValueFormat.UINT64));
    assertThat(reader.nextType()).isEqualTo(ValueType.INTEGER);
    var output = reader.readLong();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readFloat(@ForAll float input) throws IOException {
    packer.packFloat(input).flush();
    assertThat(reader.nextFormat()).isEqualTo(ValueFormat.FLOAT32);
    assertThat(reader.nextType()).isEqualTo(ValueType.FLOAT);
    var output = reader.readFloat();
    assertThat(output).isEqualTo(input);
  }

  @Property
  public void readDouble(@ForAll double input) throws IOException {
    packer.packDouble(input).flush();
    assertThat(reader.nextFormat()).isEqualTo(ValueFormat.FLOAT64);
    assertThat(reader.nextType()).isEqualTo(ValueType.FLOAT);
    var output = reader.readDouble();
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
  public void readArray(
      @ForAll boolean bool,
      @ForAll byte b,
      @ForAll short s,
      @ForAll int i,
      @ForAll long l,
      @ForAll float f,
      @ForAll double d,
      @ForAll String str)
      throws IOException {
    packer
        .packArrayHeader(9)
        .packNil()
        .packBoolean(bool)
        .packByte(b)
        .packShort(s)
        .packInt(i)
        .packLong(l)
        .packFloat(f)
        .packDouble(d)
        .packString(str)
        .flush();

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
  public void readStringArray(@ForAll List<String> strings) throws IOException {
    packer.packArrayHeader(strings.size());
    for (var str : strings) {
      packer.packString(str);
    }
    packer.flush();

    assertThat(reader.readArrayHeader()).isEqualTo(strings.size());
    for (var str : strings) {
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
      @ForAll String str)
      throws IOException {
    packer
        .packMapHeader(9)
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
        .packString(str)
        .packString(str)
        .packNil()
        .flush();

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
  public void readStringMap(@ForAll Map<String, String> strings) throws IOException {
    packer.packMapHeader(strings.size());
    for (var entry : strings.entrySet()) {
      packer.packString(entry.getKey());
      packer.packString(entry.getValue());
    }
    packer.flush();

    assertThat(reader.readMapHeader()).isEqualTo(strings.size());
    for (var entry : strings.entrySet()) {
      assertThat(reader.readString()).isEqualTo(entry.getKey());
      assertThat(reader.readString()).isEqualTo(entry.getValue());
    }
  }

  private void doReadString(String input) throws IOException {
    packer.packString(input);
    packer.flush();
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
