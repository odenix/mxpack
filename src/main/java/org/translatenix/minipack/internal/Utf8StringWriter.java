/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.translatenix.minipack.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.translatenix.minipack.MessageSink;
import org.translatenix.minipack.StringWriter;

public final class Utf8StringWriter implements StringWriter<CharSequence> {
  @Override
  public void write(CharSequence string, ByteBuffer writeBuffer, MessageSink sink)
      throws IOException {
    var length = utf8Length(string);
    if (length < 0) {
      writeHeader(-length, writeBuffer, sink);
      writeAscii(string, writeBuffer, sink);
    } else {
      writeHeader(length, writeBuffer, sink);
      writeNonAscii(string, writeBuffer, sink);
    }
  }

  public static void writeHeader(int length, ByteBuffer buffer, MessageSink sink)
      throws IOException {
    if (length < 0) throw Exceptions.invalidLength(length);
    if (length < (1 << 5)) {
      sink.ensureRemaining(buffer, 1);
      buffer.put((byte) (ValueFormat.FIXSTR_PREFIX | length));
    } else if (length < (1 << 8)) {
      sink.ensureRemaining(buffer, 2);
      buffer.put(ValueFormat.STR8);
      buffer.put((byte) length);
    } else if (length < (1 << 16)) {
      sink.ensureRemaining(buffer, 3);
      buffer.put(ValueFormat.STR16);
      buffer.putShort((short) length);
    } else {
      sink.ensureRemaining(buffer, 5);
      buffer.put(ValueFormat.STR32);
      buffer.putInt(length);
    }
  }

  private static int utf8Length(CharSequence string) {
    var length = string.length();
    var i = 0;
    for (; i < length; i++) {
      if (string.charAt(i) >= 0x80) break;
    }
    if (i == length) return -length;

    var result = i;
    for (; i < length; i++) {
      var ch = string.charAt(i);
      if (ch < 0x80) {
        result += 1;
      } else if (ch < 0x800) {
        result += 2;
      } else if (ch < Character.MIN_SURROGATE || ch > Character.MAX_SURROGATE) {
        result += 3;
      } else {
        // leave validation to writeNonAscii()
        result += 4;
        i += 1;
      }
    }
    return result;
  }

  private static void writeAscii(CharSequence string, ByteBuffer buffer, MessageSink sink)
      throws IOException {
    var length = string.length();
    var i = 0;
    while (true) { // repeat filling and writing buffer until done
      var nextStop = Math.min(length, i + buffer.remaining());
      for (; i < nextStop; i++) {
        buffer.put((byte) string.charAt(i));
      }
      if (i == length) break;
      buffer.flip();
      sink.write(buffer);
      buffer.compact();
    }
  }

  private static void writeNonAscii(CharSequence string, ByteBuffer buffer, MessageSink sink)
      throws IOException {
    var length = string.length();
    for (var i = 0; i < length; i++) {
      var ch = string.charAt(i);
      if (ch < 0x80) {
        sink.ensureRemaining(buffer, 1);
        buffer.put((byte) ch);
      } else if (ch < 0x800) {
        sink.ensureRemaining(buffer, 2);
        buffer.put((byte) (0xc0 | ch >>> 6));
        buffer.put((byte) (0x80 | (ch & 0x3f)));
      } else if (ch < Character.MIN_SURROGATE || ch > Character.MAX_SURROGATE) {
        sink.ensureRemaining(buffer, 3);
        buffer.put((byte) (0xe0 | ch >>> 12));
        buffer.put((byte) (0x80 | ((ch >>> 6) & 0x3f)));
        buffer.put((byte) (0x80 | (ch & 0x3f)));
      } else {
        char ch2;
        if (++i == length || !Character.isSurrogatePair(ch, ch2 = string.charAt(i))) {
          throw Exceptions.invalidSurrogatePair(i);
        }
        var cp = Character.toCodePoint(ch, ch2);
        sink.ensureRemaining(buffer, 4);
        buffer.put((byte) (0xf0 | cp >>> 18));
        buffer.put((byte) (0x80 | ((cp >>> 12) & 0x3f)));
        buffer.put((byte) (0x80 | ((cp >>> 6) & 0x3f)));
        buffer.put((byte) (0x80 | (cp & 0x3f)));
      }
    }
  }
}
