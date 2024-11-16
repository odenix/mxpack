package org.minipack.kotlin

import org.minipack.java.BufferAllocator
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

fun bufferAllocatorOfUnpooled(
  maxBufferCapacity: Int = 1024 * 1024,
  useDirectBuffers: Boolean = false
) = BufferAllocator.ofUnpooled { options ->
  options.maxBufferCapacity(maxBufferCapacity).useDirectBuffers(useDirectBuffers)
}

fun bufferAllocatorOfPooled(
  maxBufferCapacity: Int = 1024 * 1024,
  useDirectBuffers: Boolean = false
) = BufferAllocator.ofPooled { options ->
  options.maxBufferCapacity(maxBufferCapacity).useDirectBuffers(useDirectBuffers)
}

fun messageReaderOf(
  source: MessageSource,
  stringDecoder: MessageDecoder<String> = messageDecoderOfStrings(),
  identifierDecoder: MessageDecoder<String> = messageDecoderOfStrings()
): MessageReader = MessageReader.of(source) { options ->
  options.stringDecoder(stringDecoder).identifierDecoder(identifierDecoder)
}

fun messageWriterOf(
  sink: MessageSink,
  stringEncoder: MessageEncoder<CharSequence> = messageEncoderOfStrings(),
  identifierEncoder: MessageEncoder<in String> = messageEncoderOfStrings()
): MessageWriter = MessageWriter.of(sink) { options ->
  options.stringEncoder(stringEncoder).identifierEncoder(identifierEncoder)
}

fun messageSourceOf(
  channel: ReadableByteChannel,
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSource = MessageSource.of(channel) { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun messageSourceOf(
  stream: InputStream,
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSource = MessageSource.of(stream) { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun messageSourceOf(
  buffer: ByteBuffer,
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSource = MessageSource.of(buffer) { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun messageSourceOf(
  provider: MessageSource.Provider,
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSource = MessageSource.of(provider) { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun messageSinkOf(
  channel: WritableByteChannel,
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSink = MessageSink.of(channel) { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun messageSinkOf(
  stream: OutputStream,
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSink = MessageSink.of(stream) { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun messageSinkOfBuffer(
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSink.InMemory<ByteBuffer> = MessageSink.ofBuffer { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun messageSinkOf(
  provider: MessageSink.Provider<Void>,
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSink = MessageSink.of(provider) { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun <T: Any> messageSinkOfInMemory(
  provider: MessageSink.Provider<T>,
  allocator: BufferAllocator = bufferAllocatorOfUnpooled(),
  bufferCapacity: Int = 1024 * 8
): MessageSink.InMemory<T> = MessageSink.ofInMemory(provider) { options ->
  options.allocator(allocator).bufferCapacity(bufferCapacity)
}

fun messageEncoderOfStrings(
  charsetEncoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder()
): MessageEncoder<CharSequence> = MessageEncoder.ofStrings { options ->
  options.charsetEncoder(charsetEncoder)
}

fun messageDecoderOfStrings(
  charsetDecoder: CharsetDecoder = StandardCharsets.UTF_8.newDecoder()
): MessageDecoder<String> = MessageDecoder.ofStrings { options ->
  options.charsetDecoder(charsetDecoder)
}