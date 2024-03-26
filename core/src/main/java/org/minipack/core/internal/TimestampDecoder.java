/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import org.minipack.core.Decoder;
import org.minipack.core.MessageSource;

public final class TimestampDecoder implements Decoder<Instant> {
  public static final TimestampDecoder INSTANCE = new TimestampDecoder();

  private static final byte EXTENSION_TYPE = -1;
  private static final long LOWER_34_BITS_MASK = 0x3ffffffffL;

  private TimestampDecoder() {}

  @Override
  public Instant decode(ByteBuffer buffer, MessageSource source) throws IOException {
    var header = source.getExtensionHeader(buffer);
    if (header.type() != EXTENSION_TYPE) {
      throw Exceptions.extensionTypeMismatch(EXTENSION_TYPE, header.type());
    }
    return switch (header.length()) {
      case 4 -> Instant.ofEpochSecond(source.getInt(buffer));
      case 8 -> {
        var value = source.getLong(buffer);
        var nanos = value >>> 34;
        var seconds = value & LOWER_34_BITS_MASK;
        yield Instant.ofEpochSecond(seconds, nanos);
      }
      case 12 -> {
        var nanos = source.getInt(buffer);
        var seconds = source.getLong(buffer);
        yield Instant.ofEpochSecond(seconds, nanos);
      }
      default -> throw Exceptions.invalidTimestampLength(header.length());
    };
  }
}
