/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack;

// https://github.com/msgpack/msgpack/blob/master/spec.md#formats
final class Format {
  private Format() {}

  static final byte NIL = (byte) 0xc0;
  static final byte NEVER_USED = (byte) 0xc1;
  static final byte FALSE = (byte) 0xc2;
  static final byte TRUE = (byte) 0xc3;
  static final byte BIN8 = (byte) 0xc4;
  static final byte BIN16 = (byte) 0xc5;
  static final byte BIN32 = (byte) 0xc6;
  static final byte EXT8 = (byte) 0xc7;
  static final byte EXT16 = (byte) 0xc8;
  static final byte EXT32 = (byte) 0xc9;
  static final byte FLOAT32 = (byte) 0xca;
  static final byte FLOAT64 = (byte) 0xcb;
  static final byte UINT8 = (byte) 0xcc;
  static final byte UINT16 = (byte) 0xcd;
  static final byte UINT32 = (byte) 0xce;
  static final byte UINT64 = (byte) 0xcf;
  static final byte INT8 = (byte) 0xd0;
  static final byte INT16 = (byte) 0xd1;
  static final byte INT32 = (byte) 0xd2;
  static final byte INT64 = (byte) 0xd3;
  static final byte FIXEXT1 = (byte) 0xd4;
  static final byte FIXEXT2 = (byte) 0xd5;
  static final byte FIXEXT4 = (byte) 0xd6;
  static final byte FIXEXT8 = (byte) 0xd7;
  static final byte FIXEXT16 = (byte) 0xd8;
  static final byte STR8 = (byte) 0xd9;
  static final byte STR16 = (byte) 0xda;
  static final byte STR32 = (byte) 0xdb;
  static final byte ARRAY16 = (byte) 0xdc;
  static final byte ARRAY32 = (byte) 0xdd;
  static final byte MAP16 = (byte) 0xde;
  static final byte MAP32 = (byte) 0xdf;

  static final byte FIXSTR_PREFIX = (byte) 0b101_00000;
  static final byte FIXSTR_MASK = (byte) 0b111_00000;
  static final byte FIXARRAY_PREFIX = (byte) 0b1001_0000;
  static final byte FIXARRAY_MASK = (byte) 0b1111_0000;
  static final byte FIXMAP_PREFIX = (byte) 0b1000_0000;
  static final byte FIXMAP_MASK = (byte) 0b1111_0000;

  static boolean isFixInt(byte format) {
    return format >= -(1 << 5);
  }

  static boolean isFixStr(byte format) {
    return (format & FIXSTR_MASK) == FIXSTR_PREFIX;
  }

  static boolean isFixArray(byte format) {
    return (format & FIXARRAY_MASK) == FIXARRAY_PREFIX;
  }

  static boolean isFixMap(byte format) {
    return (format & FIXMAP_MASK) == FIXMAP_PREFIX;
  }

  static int getFixStrLength(byte format) {
    return format & ~FIXSTR_MASK;
  }

  static int getFixArrayLength(byte format) {
    return format & ~FIXARRAY_MASK;
  }

  static int getFixMapLength(byte format) {
    return format & ~FIXMAP_MASK;
  }

  static MessageType toType(byte format) {
    return switch (format) {
      case NIL -> MessageType.NIL;
      case TRUE, FALSE -> MessageType.BOOLEAN;
      case INT8, UINT8, INT16, UINT16, INT32, UINT32, INT64, UINT64 -> MessageType.INTEGER;
      case FLOAT32, FLOAT64 -> MessageType.FLOAT;
      case STR8, STR16, STR32 -> MessageType.STRING;
      case ARRAY16, ARRAY32 -> MessageType.ARRAY;
      case MAP16, MAP32 -> MessageType.MAP;
      case BIN8, BIN16, BIN32 -> MessageType.BINARY;
      case FIXEXT1, FIXEXT2, FIXEXT4, FIXEXT8, EXT8, EXT16, EXT32 -> MessageType.EXTENSION;
      default ->
          Format.isFixInt(format)
              ? MessageType.INTEGER
              : Format.isFixStr(format)
                  ? MessageType.STRING
                  : Format.isFixArray(format)
                      ? MessageType.ARRAY
                      : Format.isFixMap(format) ? MessageType.MAP : invalidFormat(format);
    };
  }

  private static MessageType invalidFormat(byte format) {
    throw ReaderException.invalidFormat(format);
  }
}
