/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.extension;

import java.io.IOException;
import org.minipack.core.MessageEncoder;
import org.minipack.core.MessageSink;
import org.minipack.core.MessageWriter;
import org.minipack.extension.internal.Exceptions;

public final class Utf8StringEncoder implements MessageEncoder<CharSequence> {
  @Override
  public void encode(CharSequence string, MessageSink sink, MessageWriter writer)
      throws IOException {
    var length = utf8Length(string);
    writer.writeStringHeader(Math.abs(length));
    encodePayload(string, sink, length < 0);
  }

  public static int utf8Length(CharSequence string) {
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

  public static void encodePayload(CharSequence string, MessageSink sink, boolean isAscii) throws IOException {
    if (isAscii) {
      encodeAscii(string, sink);
    } else {
      encodeNonAscii(string, sink);
    }
  }

  private static void encodeAscii(CharSequence string, MessageSink sink) throws IOException {
    var length = string.length();
    var buffer = sink.buffer();
    var i = 0;
    while (true) { // repeat filling and writing buffer until done
      var nextStop = Math.min(length, i + buffer.remaining());
      for (; i < nextStop; i++) {
        buffer.put((byte) string.charAt(i));
      }
      if (i == length) break;
      sink.flushBuffer();
    }
  }

  private static void encodeNonAscii(CharSequence string, MessageSink sink) throws IOException {
    var length = string.length();
    for (var i = 0; i < length; i++) {
      var ch = string.charAt(i);
      if (ch < 0x80) {
        sink.writeByte((byte) ch);
      } else if (ch < 0x800) {
        sink.writeBytes((byte) (0xc0 | ch >>> 6), (byte) (0x80 | (ch & 0x3f)));
      } else if (ch < Character.MIN_SURROGATE || ch > Character.MAX_SURROGATE) {
        sink.writeBytes(
            (byte) (0xe0 | ch >>> 12),
            (byte) (0x80 | ((ch >>> 6) & 0x3f)),
            (byte) (0x80 | (ch & 0x3f)));
      } else {
        char ch2;
        if (++i == length || !Character.isSurrogatePair(ch, ch2 = string.charAt(i))) {
          throw Exceptions.malformedSurrogate(i);
        }
        var cp = Character.toCodePoint(ch, ch2);
        sink.writeBytes(
            (byte) (0xf0 | cp >>> 18),
            (byte) (0x80 | ((cp >>> 12) & 0x3f)),
            (byte) (0x80 | ((cp >>> 6) & 0x3f)),
            (byte) (0x80 | (cp & 0x3f)));
      }
    }
  }
}
