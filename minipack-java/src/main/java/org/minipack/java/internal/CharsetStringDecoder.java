/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.java.internal;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.minipack.java.*;

public final class CharsetStringDecoder implements MessageDecoder<String> {
  private final CharsetDecoder charsetDecoder;

  private static final class Options implements MessageDecoder.StringOptions {
    private @Nullable CharsetDecoder charsetDecoder;

    @Override
    public StringOptions charsetDecoder(CharsetDecoder decoder) {
      charsetDecoder = decoder;
      return this;
    }
  }

  public CharsetStringDecoder() {
    this(options -> {});
  }

  public CharsetStringDecoder(Consumer<MessageDecoder.StringOptions> consumer) {
    var options = new Options();
    consumer.accept(options);
    charsetDecoder =
        options.charsetDecoder != null
            ? options.charsetDecoder
            : StandardCharsets.UTF_8.newDecoder();
  }

  @Override
  public String decode(MessageSource source, MessageReader reader) throws IOException {
    var byteLength = reader.readStringHeader();
    if (byteLength == 0) return "";
    var byteBuffer = source.buffer();
    try (var pooledCharBuffer =
        source.allocator().getCharBuffer(byteLength * charsetDecoder.maxCharsPerByte())) {
      var charBuffer = pooledCharBuffer.value();
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
    }
  }
}
