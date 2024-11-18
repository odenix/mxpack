package org.minipack.kotlin

import org.minipack.java.BufferAllocator
import org.minipack.java.BufferAllocator.PooledByteBuffer
import org.minipack.java.MessageDecoder
import org.minipack.java.MessageEncoder
import org.minipack.java.MessageReader
import org.minipack.java.MessageSink
import org.minipack.java.MessageSource
import org.minipack.java.MessageWriter
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder
import java.nio.charset.StandardCharsets
import java.util.function.Supplier

object BufferAllocators {
  fun ofPooled(
    maxBufferCapacity: Int = 1024 * 1024,
    useDirectBuffers: Boolean = false
  ) = BufferAllocator.ofPooled { options ->
    options.maxBufferCapacity(maxBufferCapacity).useDirectBuffers(useDirectBuffers)
  }

  fun ofUnpooled(
    maxBufferCapacity: Int = 1024 * 1024,
    useDirectBuffers: Boolean = false
  ) = BufferAllocator.ofUnpooled { options ->
    options.maxBufferCapacity(maxBufferCapacity).useDirectBuffers(useDirectBuffers)
  }
}

object MessageReaders {
  fun of(
    source: MessageSource,
    stringDecoder: MessageDecoder<String> = MessageDecoders.ofStrings(),
    identifierDecoder: MessageDecoder<String> = MessageDecoders.ofStrings()
  ): MessageReader = MessageReader.of(source) { options ->
    options.stringDecoder(stringDecoder).identifierDecoder(identifierDecoder)
  }
}

object MessageWriters {
  fun of(
    sink: MessageSink,
    stringEncoder: MessageEncoder<CharSequence> = MessageEncoders.ofStrings(),
    identifierEncoder: MessageEncoder<in String> = MessageEncoders.ofStrings()
  ): MessageWriter = MessageWriter.of(sink) { options ->
    options.stringEncoder(stringEncoder).identifierEncoder(identifierEncoder)
  }
}

object MessageSources {
  fun of(
    channel: ReadableByteChannel,
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSource = MessageSource.of(channel) { options ->
    options.allocator(allocator).bufferCapacity(bufferCapacity)
  }

  fun of(
    stream: InputStream,
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSource = MessageSource.of(stream) { options ->
    options.allocator(allocator).bufferCapacity(bufferCapacity)
  }

  fun of(
    buffer: PooledByteBuffer,
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSource = MessageSource.of(buffer) { options ->
    options.allocator(allocator).bufferCapacity(bufferCapacity)
  }

  fun of(
    buffer: ByteBuffer,
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSource = MessageSource.of(buffer) { options ->
    options.allocator(allocator).bufferCapacity(bufferCapacity)
  }

  fun of(
    provider: MessageSource.Provider,
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSource = MessageSource.of(provider) { options ->
    options.allocator(allocator).bufferCapacity(bufferCapacity)
  }
}

object MessageSinks {
  fun of(
    channel: WritableByteChannel,
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSink = MessageSink.of(channel) { options ->
    options.allocator(allocator).bufferCapacity(bufferCapacity)
  }

  fun of(
    stream: OutputStream,
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSink = MessageSink.of(stream) { options ->
    options.allocator(allocator).bufferCapacity(bufferCapacity)
  }

  fun ofBuffer(
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): Pair<MessageSink, Supplier<PooledByteBuffer>> {
    val sinkWithOutput = MessageSink.ofBuffer { options ->
      options.allocator(allocator).bufferCapacity(bufferCapacity)
    }
    return sinkWithOutput.sink to sinkWithOutput.output
  }

  fun ofDiscarding(
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSink {
    return MessageSink.ofDiscarding { options ->
      options.allocator(allocator).bufferCapacity(bufferCapacity)
    }
  }

  fun of(
    provider: MessageSink.Provider,
    allocator: BufferAllocator = BufferAllocators.ofUnpooled(),
    bufferCapacity: Int = 1024 * 8
  ): MessageSink = MessageSink.of(provider) { options ->
    options.allocator(allocator).bufferCapacity(bufferCapacity)
  }
}

object MessageEncoders {
  fun ofStrings(
    charsetEncoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder()
  ): MessageEncoder<CharSequence> = MessageEncoder.ofStrings { options ->
    options.charsetEncoder(charsetEncoder)
  }
}

object MessageDecoders {
  fun ofStrings(
    charsetDecoder: CharsetDecoder = StandardCharsets.UTF_8.newDecoder()
  ): MessageDecoder<String> = MessageDecoder.ofStrings { options ->
    options.charsetDecoder(charsetDecoder)
  }
}
