/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import org.minipack.core.*;

public final class CharsetStringDecoder implements MessageDecoder<String> {
  private final CharsetDecoder charsetDecoder;

  public CharsetStringDecoder(CharsetDecoder charsetDecoder) {
    this.charsetDecoder = charsetDecoder;
  }

  @Override
  public String decode(MessageSource source, MessageReader reader) throws IOException {
    var byteLength = reader.readStringHeader();
    if (byteLength == 0) return "";
    var bytesLeft = byteLength;
    var byteBuffer = source.buffer();
    var charBuffer = source.allocator().charBuffer(byteLength * charsetDecoder.maxCharsPerByte());
    try {
      charsetDecoder.reset();
      while (true) {
        var remaining = byteBuffer.remaining();
        CoderResult result;
        if (bytesLeft <= remaining) {
          var savedLimit = byteBuffer.limit();
          byteBuffer.limit(byteBuffer.position() + bytesLeft);
          result = charsetDecoder.decode(byteBuffer, charBuffer, true);
          byteBuffer.limit(savedLimit);
          if (result.isUnderflow()) break;
        } else {
          result = charsetDecoder.decode(byteBuffer, charBuffer, false);
          bytesLeft -= (remaining - byteBuffer.remaining());
          if (result.isUnderflow()) {
            var bytesRead = source.read(byteBuffer.compact());
            if (bytesRead == -1) {
              throw Exceptions.prematureEndOfInput(byteLength, byteLength - bytesLeft);
            }
            byteBuffer.flip();
            continue;
          }
        }
        if (result.isError()) {
          throw Exceptions.codingError(result, charBuffer.position());
        }
        assert result.isOverflow();
        throw Exceptions.unreachableCode();
      }
      charsetDecoder.flush(charBuffer);
      return charBuffer.flip().toString();
    } finally {
      source.allocator().release(charBuffer);
    }
  }
}
