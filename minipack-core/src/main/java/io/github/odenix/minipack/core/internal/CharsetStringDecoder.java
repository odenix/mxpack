/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core.internal;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import io.github.odenix.minipack.core.MessageDecoder;
import io.github.odenix.minipack.core.MessageSource;

/// A string decoder backed by [CharsetDecoder].
public final class CharsetStringDecoder implements MessageDecoder<String> {
  // CharsetDecoder is up to 10x faster than an optimized hand-written UTF-8 decoder.
  // https://cl4es.github.io/2021/02/23/Faster-Charset-Decoding.html
  //
  // Potential optimization opportunities:
  // * support UTF-16
  // * use Java Vector API
  // * support TruffleString (for Truffle languages)
  private final CharsetDecoder charsetDecoder =
      StandardCharsets.UTF_8.newDecoder()
          .onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .replaceWith("\ufffd"); // default

  @Override
  public String decode(MessageSource source) throws IOException {
    var byteLength = MessageReaderImpl.readStringHeader(source);
    if (byteLength == 0) return "";
    var byteBuffer = source.buffer();
    try (var leasedCharBuffer = source.allocator().getCharBuffer(byteLength)) {
      var charBuffer = leasedCharBuffer.get();
      charsetDecoder.reset();
      var bytesLeft = byteLength;
      int remaining;
      while (bytesLeft > (remaining = byteBuffer.remaining())) {
        var result = charsetDecoder.decode(byteBuffer, charBuffer, false);
        assert !result.isError(); // we use CodingErrorAction.REPLACE
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
      assert !result.isError(); // we use CodingErrorAction.REPLACE
      byteBuffer.limit(savedLimit);
      charsetDecoder.flush(charBuffer);
      return charBuffer.flip().toString();
    }
  }
}
