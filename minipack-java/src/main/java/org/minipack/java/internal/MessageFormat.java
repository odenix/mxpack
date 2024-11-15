/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.util.Arrays;
import org.minipack.java.MessageType;

/**
 * The <a href="https://github.com/msgpack/msgpack/blob/master/spec.md#formats">format</a> of a
 * MessagePack value.
 */
public final class MessageFormat {
  private static final MessageType[] typeLookup;

  private MessageFormat() {}

  public static final byte NIL = (byte) 0xc0;
  public static final byte NEVER_USED = (byte) 0xc1;
  public static final byte FALSE = (byte) 0xc2;
  public static final byte TRUE = (byte) 0xc3;
  public static final byte BIN8 = (byte) 0xc4;
  public static final byte BIN16 = (byte) 0xc5;
  public static final byte BIN32 = (byte) 0xc6;
  public static final byte EXT8 = (byte) 0xc7;
  public static final byte EXT16 = (byte) 0xc8;
  public static final byte EXT32 = (byte) 0xc9;
  public static final byte FLOAT32 = (byte) 0xca;
  public static final byte FLOAT64 = (byte) 0xcb;
  public static final byte UINT8 = (byte) 0xcc;
  public static final byte UINT16 = (byte) 0xcd;
  public static final byte UINT32 = (byte) 0xce;
  public static final byte UINT64 = (byte) 0xcf;
  public static final byte INT8 = (byte) 0xd0;
  public static final byte INT16 = (byte) 0xd1;
  public static final byte INT32 = (byte) 0xd2;
  public static final byte INT64 = (byte) 0xd3;
  public static final byte FIXEXT1 = (byte) 0xd4;
  public static final byte FIXEXT2 = (byte) 0xd5;
  public static final byte FIXEXT4 = (byte) 0xd6;
  public static final byte FIXEXT8 = (byte) 0xd7;
  public static final byte FIXEXT16 = (byte) 0xd8;
  public static final byte STR8 = (byte) 0xd9;
  public static final byte STR16 = (byte) 0xda;
  public static final byte STR32 = (byte) 0xdb;
  public static final byte ARRAY16 = (byte) 0xdc;
  public static final byte ARRAY32 = (byte) 0xdd;
  public static final byte MAP16 = (byte) 0xde;
  public static final byte MAP32 = (byte) 0xdf;

  public static final byte FIXSTR_PREFIX = (byte) 0b101_00000;
  public static final byte FIXSTR_MASK = (byte) 0b111_00000;
  public static final byte FIXARRAY_PREFIX = (byte) 0b1001_0000;
  public static final byte FIXARRAY_MASK = (byte) 0b1111_0000;
  public static final byte FIXMAP_PREFIX = (byte) 0b1000_0000;
  public static final byte FIXMAP_MASK = (byte) 0b1111_0000;

  static {
    typeLookup = new MessageType[256];
    fill(0x00, 0x7f, MessageType.INTEGER); // positive fixint
    fill(0x80, 0x8f, MessageType.MAP); // fixmap
    fill(0x90, 0x9f, MessageType.ARRAY); // fixarray
    fill(0xa0, 0xbf, MessageType.STRING); // fixstr
    typeLookup[0xc0] = MessageType.NIL;
    // 0xc1: never used
    fill(0xc2, 0xc3, MessageType.BOOLEAN);
    fill(0xc4, 0xc6, MessageType.BINARY);
    fill(0xc7, 0xc9, MessageType.EXTENSION);
    fill(0xca, 0xcb, MessageType.FLOAT);
    fill(0xcc, 0xd3, MessageType.INTEGER);
    fill(0xd4, 0xd8, MessageType.EXTENSION); // fixext
    fill(0xd9, 0xdb, MessageType.STRING);
    fill(0xdc, 0xdd, MessageType.ARRAY);
    fill(0xde, 0xdf, MessageType.MAP);
    fill(0xe0, 0xff, MessageType.INTEGER); // negative fixint
  }

  public static boolean isFixInt(byte format) {
    return format >= -(1 << 5);
  }

  public static boolean isFixStr(byte format) {
    return (format & FIXSTR_MASK) == FIXSTR_PREFIX;
  }

  public static boolean isFixArray(byte format) {
    return (format & FIXARRAY_MASK) == FIXARRAY_PREFIX;
  }

  public static boolean isFixMap(byte format) {
    return (format & FIXMAP_MASK) == FIXMAP_PREFIX;
  }

  public static int getFixStrLength(byte format) {
    return format & ~FIXSTR_MASK;
  }

  public static int getFixArrayLength(byte format) {
    return format & ~FIXARRAY_MASK;
  }

  public static int getFixMapLength(byte format) {
    return format & ~FIXMAP_MASK;
  }

  public static MessageType toType(byte format) {
    if (format == NEVER_USED) throw Exceptions.invalidMessageFormat(format);
    return typeLookup[format & 0xff];
  }

  private static void fill(int from, int until, MessageType type) {
    Arrays.fill(typeLookup, from, until + 1, type);
  }
}
