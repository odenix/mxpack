/*
 * Copyright 2024 the minipack project authors
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
    var allocator = source.allocator();
    var byteBuffer = source.buffer();
    var charBuffer = allocator.charBuffer(byteLength * (long) charsetDecoder.maxCharsPerByte());
    charsetDecoder.reset();
    var bytesLeft = byteLength;
    while (true) {
      var chunkSize = Math.min(bytesLeft, byteBuffer.remaining());
      var isLastChunk = chunkSize == bytesLeft;
      byteBuffer.limit(byteBuffer.position() + chunkSize);
      var result = charsetDecoder.decode(byteBuffer, charBuffer, isLastChunk);
      if (result.isUnderflow()) {
        if (isLastChunk) break;
        var bytesRead = source.readAny(byteBuffer, 1);
        if (bytesRead == -1) {
          throw Exceptions.prematureEndOfInput(byteLength, byteLength - bytesLeft);
        }
        bytesLeft -= bytesRead;
        continue;
      }
      if (result.isError()) {
        throw Exceptions.codingError(result, byteLength - bytesLeft);
      }
      assert result.isOverflow();
      throw Exceptions.unreachableCode();
    }
    charsetDecoder.flush(charBuffer);
    var result = charBuffer.toString();
    allocator.release(charBuffer);
    return result;
  }
}
