/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import org.odenix.mxpack.core.*

/**
 * Returns a new buffer allocator that allocates a new buffer each time
 * [BufferAllocator.getByteBuffer] or [BufferAllocator.getCharBuffer] is called.
 *
 * For additional documentation of method parameters, see [BufferAllocator.UnpooledOptionBuilder].
 */
fun UnpooledAllocator(
  /** The maximum capacity, in bytes, that may be requested by [BufferAllocator.getByteBuffer]. */
  maxByteBufferCapacity: Int = Int.MAX_VALUE,
  /** The maximum capacity, in chars, that may be requested by [BufferAllocator.getCharBuffer]. */
  maxCharBufferCapacity: Int = Int.MAX_VALUE,
): BufferAllocator =
  BufferAllocator.ofUnpooled { options ->
    options
      .maxByteBufferCapacity(maxByteBufferCapacity)
      .maxCharBufferCapacity(maxCharBufferCapacity)
  }

/**
 * Returns a new buffer allocator that maintains a buffer pool to reduce buffer allocations.
 *
 * For additional documentation of method parameters, see [BufferAllocator.PooledOptionBuilder].
 */
fun PooledAllocator(
  /** The maximum capacity, in bytes, that may be requested by [BufferAllocator.getByteBuffer]. */
  maxByteBufferCapacity: Int = Int.MAX_VALUE,
  /** The maximum capacity, in chars, that may be requested by [BufferAllocator.getCharBuffer]. */
  maxCharBufferCapacity: Int = Int.MAX_VALUE,
  /** The maximum capacity, in bytes, of a pooled byte buffer. */
  maxPooledByteBufferCapacity: Int = 1024 * 1024,
  /** The maximum capacity, in chars, of a pooled char buffer. */
  maxPooledCharBufferCapacity: Int = 1024 * 512,
  /** The maximum capacity of the byte buffer pool. */
  maxByteBufferPoolCapacity: Int = 1024 * 1024 * 64,
  /** The maximum capacity of the char buffer pool. */
  maxCharBufferPoolCapacity: Int = 1024 * 1024 * 32,
  /** Whether to preferably allocate [direct][ByteBuffer.isDirect] byte buffers. */
  preferDirectBuffers: Boolean = false,
): BufferAllocator =
  BufferAllocator.ofPooled { options ->
    options
      .maxByteBufferCapacity(maxByteBufferCapacity)
      .maxCharBufferCapacity(maxCharBufferCapacity)
      .maxPooledByteBufferCapacity(maxPooledByteBufferCapacity)
      .maxPooledCharBufferCapacity(maxPooledCharBufferCapacity)
      .maxByteBufferPoolCapacity(maxByteBufferPoolCapacity)
      .maxCharBufferPoolCapacity(maxCharBufferPoolCapacity)
      .preferDirectBuffers(preferDirectBuffers)
  }

/**
 * Returns a new [MessageOutput] for a [LeasedByteBuffer].
 *
 * For additional documentation of method parameters, see [MessageOutput.Buffer.OptionBuilder].
 */
fun BufferOutput(
  /** The initial capacity, in bytes, of the byte buffer to be produced. */
  initialCapacity: Int = 1024
): MessageOutput.Buffer =
  MessageOutput.ofBuffer { options -> options.initialCapacity(initialCapacity) }

/**
 * Returns a new message reader that reads from the given channel.
 *
 * For additional documentation of method parameters, see [MessageReader.OptionBuilder].
 */
fun MessageReader(
  /** The channel to read from. */
  channel: ReadableByteChannel,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
  readBufferCapacity: Int = 1024 * 8,
  /** The string decoder to be used by [MessageReader.readString]. */
  stringDecoder: MessageDecoder<String> = StringDecoder(),
  /** The string decoder to be used by [MessageReader.readIdentifier]. */
  identifierDecoder: MessageDecoder<String> = StringDecoder(),
): MessageReader =
  MessageReader.of(channel) { options ->
    options
      .allocator(allocator)
      .readBufferCapacity(readBufferCapacity)
      .stringDecoder(stringDecoder)
      .identifierDecoder(identifierDecoder)
  }

/**
 * Returns a new message reader that reads from the given input stream.
 *
 * For additional documentation of method parameters, see [MessageReader.OptionBuilder].
 */
fun MessageReader(
  /** The input stream to read from. */
  stream: InputStream,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
  readBufferCapacity: Int = 1024 * 8,
  /** The string decoder to be used by [MessageReader.readString]. */
  stringDecoder: MessageDecoder<String> = StringDecoder(),
  /** The string decoder to be used by [MessageReader.readIdentifier]. */
  identifierDecoder: MessageDecoder<String> = StringDecoder(),
): MessageReader =
  MessageReader.of(stream) { options ->
    options
      .allocator(allocator)
      .readBufferCapacity(readBufferCapacity)
      .stringDecoder(stringDecoder)
      .identifierDecoder(identifierDecoder)
  }

/**
 * Returns a new message reader that reads from the given byte buffer.
 *
 * For additional documentation of method parameters, see [MessageReader.OptionBuilder].
 */
fun MessageReader(
  /** The byte buffer to read from. */
  buffer: LeasedByteBuffer,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
  readBufferCapacity: Int = 1024 * 8,
  /** The string decoder to be used by [MessageReader.readString]. */
  stringDecoder: MessageDecoder<String> = StringDecoder(),
  /** The string decoder to be used by [MessageReader.readIdentifier]. */
  identifierDecoder: MessageDecoder<String> = StringDecoder(),
): MessageReader =
  MessageReader.of(buffer) { options ->
    options
      .allocator(allocator)
      .readBufferCapacity(readBufferCapacity)
      .stringDecoder(stringDecoder)
      .identifierDecoder(identifierDecoder)
  }

/**
 * Returns a new message reader that reads from the given byte buffer.
 *
 * For additional documentation of method parameters, see [MessageReader.OptionBuilder].
 */
fun MessageReader(
  /** The byte buffer to read from. */
  buffer: ByteBuffer,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
  readBufferCapacity: Int = 1024 * 8,
  /** The string decoder to be used by [MessageReader.readString]. */
  stringDecoder: MessageDecoder<String> = StringDecoder(),
  /** The string decoder to be used by [MessageReader.readIdentifier]. */
  identifierDecoder: MessageDecoder<String> = StringDecoder(),
): MessageReader =
  MessageReader.of(buffer) { options ->
    options
      .allocator(allocator)
      .readBufferCapacity(readBufferCapacity)
      .stringDecoder(stringDecoder)
      .identifierDecoder(identifierDecoder)
  }

/**
 * Returns a new message reader that reads from the given sink provider.
 *
 * For additional documentation of method parameters, see [MessageReader.OptionBuilder].
 */
fun MessageReader(
  /** The source provider to read from. */
  provider: MessageSource.Provider,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
  readBufferCapacity: Int = 1024 * 8,
  /** The string decoder to be used by [MessageReader.readString]. */
  stringDecoder: MessageDecoder<String> = StringDecoder(),
  /** The string decoder to be used by [MessageReader.readIdentifier]. */
  identifierDecoder: MessageDecoder<String> = StringDecoder(),
): MessageReader =
  MessageReader.of(provider) { options ->
    options
      .allocator(allocator)
      .readBufferCapacity(readBufferCapacity)
      .stringDecoder(stringDecoder)
      .identifierDecoder(identifierDecoder)
  }

/** Returns a new message reader that will throw [EOFException] when a value is read. */
fun EmptyMessageReader(): MessageReader = MessageReader.ofEmpty()

/**
 * Returns a new message writer that writes to the given channel.
 *
 * For additional documentation of method parameters, see [MessageWriter.OptionBuilder].
 */
fun MessageWriter(
  /** The channel to write to. */
  channel: WritableByteChannel,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message writer's [write buffer][MessageSink.buffer]. */
  writeBufferCapacity: Int = 1024 * 8,
  /** The string encoder to be used by `MessageWriter.write(CharSequence)`. */
  stringEncoder: MessageEncoder<CharSequence> = StringEncoder(),
  /** The string encoder to be used by [MessageWriter.writeIdentifier]. */
  identifierEncoder: MessageEncoder<in String> = StringEncoder(),
): MessageWriter =
  MessageWriter.of(channel) { options ->
    options
      .allocator(allocator)
      .writeBufferCapacity(writeBufferCapacity)
      .stringEncoder(stringEncoder)
      .identifierEncoder(identifierEncoder)
  }

/**
 * Returns a new message writer that writes to the given output stream.
 *
 * For additional documentation of method parameters, see [MessageWriter.OptionBuilder].
 */
fun MessageWriter(
  /** The output stream to write to. */
  stream: OutputStream,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message writer's [write buffer][MessageSink.buffer]. */
  writeBufferCapacity: Int = 1024 * 8,
  /** The string encoder to be used by `MessageWriter.write(CharSequence)`. */
  stringEncoder: MessageEncoder<CharSequence> = StringEncoder(),
  /** The string encoder to be used by [MessageWriter.writeIdentifier]. */
  identifierEncoder: MessageEncoder<in String> = StringEncoder(),
): MessageWriter =
  MessageWriter.of(stream) { options ->
    options
      .allocator(allocator)
      .writeBufferCapacity(writeBufferCapacity)
      .stringEncoder(stringEncoder)
      .identifierEncoder(identifierEncoder)
  }

/**
 * Returns a new message writer that writes to the given buffer output.
 *
 * For additional documentation of method parameters, see [MessageWriter.OptionBuilder].
 */
fun MessageWriter(
  /** The buffer output to write to. */
  output: MessageOutput.Buffer,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message writer's [write buffer][MessageSink.buffer]. */
  writeBufferCapacity: Int = 1024 * 8,
  /** The string encoder to be used by `MessageWriter.write(CharSequence)`. */
  stringEncoder: MessageEncoder<CharSequence> = StringEncoder(),
  /** The string encoder to be used by [MessageWriter.writeIdentifier]. */
  identifierEncoder: MessageEncoder<in String> = StringEncoder(),
): MessageWriter =
  MessageWriter.of(output) { options ->
    options
      .allocator(allocator)
      .writeBufferCapacity(writeBufferCapacity)
      .stringEncoder(stringEncoder)
      .identifierEncoder(identifierEncoder)
  }

/**
 * Returns a new message writer that writes to the given sink provider.
 *
 * For additional documentation of method parameters, see [MessageWriter.OptionBuilder].
 */
fun MessageWriter(
  /** The sink provider to write to. */
  provider: MessageSink.Provider,
  /** The buffer allocator to be used by the message writer. */
  allocator: BufferAllocator = UnpooledAllocator(),
  /** The capacity of the message writer's [write buffer][MessageSink.buffer]. */
  writeBufferCapacity: Int = 1024 * 8,
  /** The string encoder to be used by `MessageWriter.write(CharSequence)`. */
  stringEncoder: MessageEncoder<CharSequence> = StringEncoder(),
  /** The string encoder to be used by [MessageWriter.writeIdentifier]. */
  identifierEncoder: MessageEncoder<in String> = StringEncoder(),
): MessageWriter =
  MessageWriter.of(provider) { options ->
    options
      .allocator(allocator)
      .writeBufferCapacity(writeBufferCapacity)
      .stringEncoder(stringEncoder)
      .identifierEncoder(identifierEncoder)
  }

/** Returns a new message writer that discards any bytes written. */
fun DiscardingMessageWriter(): MessageWriter = MessageWriter.ofDiscarding()

/** Returns a new message encoder that encodes strings. */
fun StringEncoder(): MessageEncoder<CharSequence> = MessageEncoder.ofString()

/** Returns a new message decoder that decodes strings. */
fun StringDecoder(): MessageDecoder<String> = MessageDecoder.ofString()
