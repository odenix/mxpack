/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
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
    var byteBuffer = source.buffer();
    var charBuffer = source.allocator().charBuffer(byteLength * charsetDecoder.maxCharsPerByte());
    try {
      charsetDecoder.reset();
      var bytesLeft = byteLength;
      int remaining;
      while (bytesLeft > (remaining = byteBuffer.remaining())) {
        var result = charsetDecoder.decode(byteBuffer, charBuffer, false);
        if (result.isError()) throw Exceptions.stringDecodingError(result, byteBuffer);
        bytesLeft -= (remaining - byteBuffer.remaining());
        var bytesRead = source.read(byteBuffer.compact());
        if (bytesRead == -1) {
          throw Exceptions.unexpectedEndOfInput(bytesLeft);
        }
        byteBuffer.flip();
      }
      var savedLimit = byteBuffer.limit();
      byteBuffer.limit(byteBuffer.position() + bytesLeft);
      var result = charsetDecoder.decode(byteBuffer, charBuffer, true);
      if (result.isError()) throw Exceptions.stringDecodingError(result, byteBuffer);
      byteBuffer.limit(savedLimit);
      charsetDecoder.flush(charBuffer);
      return charBuffer.flip().toString();
    } finally {
      source.allocator().release(charBuffer);
    }
  }
}
