/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.minipack.core.Encoder;
import org.minipack.core.MessageSink;
import org.minipack.core.MessageWriter;

public final class StringEncoder implements Encoder<CharSequence> {
  private final int maxStringSize;

  public StringEncoder(int maxStringSize) {
    this.maxStringSize = maxStringSize;
  }

  @Override
  public void encode(CharSequence string, ByteBuffer buffer, MessageSink sink, MessageWriter writer)
      throws IOException {
    var length = utf8Length(string);
    if (length > maxStringSize) {
      throw Exceptions.stringTooLargeOnWrite(length, maxStringSize);
    }
    if (length < 0) {
      writer.writeStringHeader(-length);
      encodeAscii(string, buffer, sink);
    } else {
      writer.writeStringHeader(length);
      encodeNonAscii(string, buffer, sink);
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

  private static void encodeAscii(CharSequence string, ByteBuffer buffer, MessageSink sink)
      throws IOException {
    var length = string.length();
    var i = 0;
    while (true) { // repeat filling and writing buffer until done
      var nextStop = Math.min(length, i + buffer.remaining());
      for (; i < nextStop; i++) {
        buffer.put((byte) string.charAt(i));
      }
      if (i == length) break;
      sink.write(buffer);
    }
  }

  private static void encodeNonAscii(CharSequence string, ByteBuffer buffer, MessageSink sink)
      throws IOException {
    var length = string.length();
    for (var i = 0; i < length; i++) {
      var ch = string.charAt(i);
      if (ch < 0x80) {
        sink.putByte(buffer, (byte) ch);
      } else if (ch < 0x800) {
        sink.putBytes(buffer, (byte) (0xc0 | ch >>> 6), (byte) (0x80 | (ch & 0x3f)));
      } else if (ch < Character.MIN_SURROGATE || ch > Character.MAX_SURROGATE) {
        sink.putBytes(
            buffer,
            (byte) (0xe0 | ch >>> 12),
            (byte) (0x80 | ((ch >>> 6) & 0x3f)),
            (byte) (0x80 | (ch & 0x3f)));
      } else {
        char ch2;
        if (++i == length || !Character.isSurrogatePair(ch, ch2 = string.charAt(i))) {
          throw Exceptions.invalidSurrogatePair(i);
        }
        var cp = Character.toCodePoint(ch, ch2);
        sink.putBytes(
            buffer,
            (byte) (0xf0 | cp >>> 18),
            (byte) (0x80 | ((cp >>> 12) & 0x3f)),
            (byte) (0x80 | ((cp >>> 6) & 0x3f)),
            (byte) (0x80 | (cp & 0x3f)));
      }
    }
  }
}
