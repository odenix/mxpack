/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin.example

import org.odenix.mxpack.core.MessageReader
import org.odenix.mxpack.core.MessageWriter
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

abstract class Example {
  val outStream: PipedOutputStream = PipedOutputStream()
  val inStream: InputStream = PipedInputStream(outStream, 1024 * 20)
  val outChannel: WritableByteChannel by lazy { Channels.newChannel(outStream) }
  val inChannel: ReadableByteChannel by lazy { Channels.newChannel(inStream) }
  val writer: MessageWriter by lazy { MessageWriter.of(outStream) }
  val reader: MessageReader by lazy { MessageReader.of(inStream) }
}
