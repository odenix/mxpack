/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import org.minipack.core.Encoder;
import org.minipack.core.MessageSink;

public final class TimestampEncoder implements Encoder<Instant> {
  public static final TimestampEncoder INSTANCE = new TimestampEncoder();

  private static final byte EXTENSION_TYPE = -1;

  private TimestampEncoder() {}

  @Override
  public void encode(Instant value, ByteBuffer buffer, MessageSink sink) throws IOException {
    var seconds = value.getEpochSecond();
    var nanos = value.getNano();
    if (nanos == 0 && seconds >= 0 && seconds < (1L << 32)) {
      sink.putExtensionHeader(buffer, 4, EXTENSION_TYPE);
      sink.putInt(buffer, (int) seconds);
    } else if (seconds >= 0 && seconds < (1L << 34)) {
      sink.putExtensionHeader(buffer, 8, EXTENSION_TYPE);
      sink.putLong(buffer, ((long) nanos) << 34 | seconds);
    } else {
      sink.putExtensionHeader(buffer, 12, EXTENSION_TYPE);
      sink.putInt(buffer, nanos);
      sink.putLong(buffer, seconds);
    }
  }
}
