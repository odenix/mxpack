/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import org.minipack.core.*
import org.minipack.core.MessageWriter.BufferOutput

/** Factory methods for constructing [BufferAllocator]s. */
object BufferAllocators {
  /**
   * Returns a new buffer allocator that allocates a new buffer each time
   * [BufferAllocator.getByteBuffer] or [BufferAllocator.getCharBuffer] is called.
   *
   * For further documentation of method parameters, see [BufferAllocator.UnpooledOptionBuilder].
   */
  fun ofUnpooled(
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
   * For further documentation of method parameters, see [BufferAllocator.PooledOptionBuilder].
   */
  fun ofPooled(
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
}

/** Factory methods for constructing [BufferOutput]s. */
object BufferOutputs {
  /** Returns a new buffer output. */
  fun of(): BufferOutput = BufferOutput.of()
}

/** Factory methods for constructing [MessageReader]s. */
object MessageReaders {
  /**
   * Returns a new message reader that reads from the given channel.
   *
   * For further documentation of method parameters, see [MessageReader.OptionBuilder].
   */
  fun of(
    /** The channel to read from. */
    channel: ReadableByteChannel,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
    readBufferCapacity: Int = 1024 * 8,
    /** The string decoder to be used by [MessageReader.readString]. */
    stringDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
    /** The string decoder to be used by [MessageReader.readIdentifier]. */
    identifierDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
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
   * For further documentation of method parameters, see [MessageReader.OptionBuilder].
   */
  fun of(
    /** The input stream to read from. */
    stream: InputStream,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
    readBufferCapacity: Int = 1024 * 8,
    /** The string decoder to be used by [MessageReader.readString]. */
    stringDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
    /** The string decoder to be used by [MessageReader.readIdentifier]. */
    identifierDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
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
   * For further documentation of method parameters, see [MessageReader.OptionBuilder].
   */
  fun of(
    /** The byte buffer to read from. */
    buffer: LeasedByteBuffer,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
    readBufferCapacity: Int = 1024 * 8,
    /** The string decoder to be used by [MessageReader.readString]. */
    stringDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
    /** The string decoder to be used by [MessageReader.readIdentifier]. */
    identifierDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
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
   * For documentation of method parameters, see [MessageReader.OptionBuilder].
   */
  fun of(
    /** The byte buffer to read from. */
    buffer: ByteBuffer,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
    readBufferCapacity: Int = 1024 * 8,
    /** The string decoder to be used by [MessageReader.readString]. */
    stringDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
    /** The string decoder to be used by [MessageReader.readIdentifier]. */
    identifierDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
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
   * For documentation of method parameters, see [MessageReader.OptionBuilder].
   */
  fun of(
    /** The source provider to read from. */
    provider: MessageSource.Provider,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message reader's [read buffer][MessageSink.buffer]. */
    readBufferCapacity: Int = 1024 * 8,
    /** The string decoder to be used by [MessageReader.readString]. */
    stringDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
    /** The string decoder to be used by [MessageReader.readIdentifier]. */
    identifierDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
  ): MessageReader =
    MessageReader.of(provider) { options ->
      options
        .allocator(allocator)
        .readBufferCapacity(readBufferCapacity)
        .stringDecoder(stringDecoder)
        .identifierDecoder(identifierDecoder)
    }

  /** Returns a new message reader that will throw [EOFException] when a value is read. */
  fun ofEmpty(): MessageReader = MessageReader.ofEmpty()
}

/** Factory methods for constructing [MessageWriter]s. */
object MessageWriters {
  /**
   * Returns a new message writer that writes to the given channel.
   *
   * For further documentation of method parameters, see [MessageWriter.OptionBuilder].
   */
  fun of(
    /** The channel to write to. */
    channel: WritableByteChannel,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message writer's [write buffer][MessageSink.buffer]. */
    writeBufferCapacity: Int = 1024 * 8,
    /** The string encoder to be used by `MessageWriter.write(CharSequence)`. */
    stringEncoder: MessageEncoder<CharSequence> = MessageEncoders.ofStrings(),
    /** The string encoder to be used by [MessageWriter.writeIdentifier]. */
    identifierEncoder: MessageEncoder<in String> = MessageEncoders.ofStrings(),
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
   * For further documentation of method parameters, see [MessageWriter.OptionBuilder].
   */
  fun of(
    /** The output stream to write to. */
    stream: OutputStream,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message writer's [write buffer][MessageSink.buffer]. */
    writeBufferCapacity: Int = 1024 * 8,
    /** The string encoder to be used by `MessageWriter.write(CharSequence)`. */
    stringEncoder: MessageEncoder<CharSequence> = MessageEncoders.ofStrings(),
    /** The string encoder to be used by [MessageWriter.writeIdentifier]. */
    identifierEncoder: MessageEncoder<in String> = MessageEncoders.ofStrings(),
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
   * For documentation of method parameters, see [MessageWriter.OptionBuilder].
   */
  fun of(
    /** The buffer output to write to. */
    output: BufferOutput,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message writer's [write buffer][MessageSink.buffer]. */
    writeBufferCapacity: Int = 1024 * 8,
    /** The string encoder to be used by `MessageWriter.write(CharSequence)`. */
    stringEncoder: MessageEncoder<CharSequence> = MessageEncoders.ofStrings(),
    /** The string encoder to be used by [MessageWriter.writeIdentifier]. */
    identifierEncoder: MessageEncoder<in String> = MessageEncoders.ofStrings(),
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
   * For further documentation of method parameters, see [MessageWriter.OptionBuilder].
   */
  fun of(
    /** The sink provider to write to. */
    provider: MessageSink.Provider,
    /** The buffer allocator to be used by the message writer. */
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    /** The capacity of the message writer's [write buffer][MessageSink.buffer]. */
    writeBufferCapacity: Int = 1024 * 8,
    /** The string encoder to be used by `MessageWriter.write(CharSequence)`. */
    stringEncoder: MessageEncoder<CharSequence> = MessageEncoders.ofStrings(),
    /** The string encoder to be used by [MessageWriter.writeIdentifier]. */
    identifierEncoder: MessageEncoder<in String> = MessageEncoders.ofStrings(),
  ): MessageWriter =
    MessageWriter.of(provider) { options ->
      options
        .allocator(allocator)
        .writeBufferCapacity(writeBufferCapacity)
        .stringEncoder(stringEncoder)
        .identifierEncoder(identifierEncoder)
    }

  /** Returns a new message writer that discards any bytes written. */
  fun ofDiscarding(): MessageWriter = MessageWriter.ofDiscarding()
}

/** Factory methods for constructing [MessageEncoder]s. */
object MessageEncoders {
  /** Returns a new message encoder that encodes strings. */
  fun ofStrings(): MessageEncoder<CharSequence> = MessageEncoder.ofStrings()
}

/** Factory methods for constructing [MessageDecoder]s. */
object MessageDecoders {
  /** Returns a new message decoder that decodes strings. */
  fun ofStrings(): MessageDecoder<String> = MessageDecoder.ofStrings()
}
