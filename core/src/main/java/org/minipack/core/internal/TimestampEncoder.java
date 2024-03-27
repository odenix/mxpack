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
import org.minipack.core.MessageWriter;

public final class TimestampEncoder implements Encoder<Instant> {
  public static final TimestampEncoder INSTANCE = new TimestampEncoder();

  private static final byte EXTENSION_TYPE = -1;

  private TimestampEncoder() {}

  @Override
  public void encode(Instant value, ByteBuffer buffer, MessageSink sink, MessageWriter writer)
      throws IOException {
    var seconds = value.getEpochSecond();
    var nanos = value.getNano();
    if (nanos == 0 && seconds >= 0 && seconds < (1L << 32)) {
      writer.writeExtensionHeader(4, EXTENSION_TYPE);
      sink.putInt(buffer, (int) seconds);
    } else if (seconds >= 0 && seconds < (1L << 34)) {
      writer.writeExtensionHeader(8, EXTENSION_TYPE);
      sink.putLong(buffer, ((long) nanos) << 34 | seconds);
    } else {
      writer.writeExtensionHeader(12, EXTENSION_TYPE);
      sink.putInt(buffer, nanos);
      sink.putLong(buffer, seconds);
    }
  }
}
