/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;
import java.util.function.Consumer;

import org.odenix.mxpack.core.internal.MessageWriterImpl;

/// Writes values encoded in MessagePack to a channel, output stream, [MessageOutput], or [MessageSink.Provider].
///
/// To create a new message writer, call [MessageWriter#of].
/// To write values, call the various [#write] and `writeXxx` methods.
/// To flush a message writer, call [#flush()].
/// To close a message writer, call [#close()].
/// **Continued use of a closed message writer can result in data corruption**.
///
/// For usage examples, see [Writing data](https://odenix.org/mxpack/examples/#writing-data).
public sealed interface MessageWriter extends Closeable permits MessageWriterImpl {
  /// A builder of message writer options.
  sealed interface OptionBuilder permits MessageWriterImpl.OptionBuilderImpl {
    /// Sets the buffer allocator to be used by the message writer.
    ///
    /// Default: [BufferAllocator#ofUnpooled()]
    ///
    /// @param allocator the buffer allocator to be used by the message writer
    /// @return this builder
    OptionBuilder allocator(BufferAllocator allocator);

    /// Sets the capacity of the message writer's [write buffer][MessageSink#buffer()].
    ///
    /// Default: `1024 * 8`
    ///
    /// @param capacity the capacity of the message writer's write buffer
    /// @return this builder`
    OptionBuilder writeBufferCapacity(int capacity);

    /// Sets the string encoder to be used by [MessageWriter#write(CharSequence)].
    ///
    /// Default: [MessageEncoder#ofStrings()]
    ///
    /// @param encoder the string encoder to be used by [MessageWriter#write(CharSequence)]
    /// @return this builder
    OptionBuilder stringEncoder(MessageEncoder<CharSequence> encoder);

    /// Sets the string encoder to be used by [MessageWriter#writeIdentifier].
    ///
    /// Default: [MessageEncoder#ofStrings()]
    ///
    /// @param encoder the string encoder to be used by [MessageWriter#writeIdentifier]
    /// @return this builder
    OptionBuilder identifierEncoder(MessageEncoder<? super String> encoder);
  }

  /// Returns a new message writer that writes to the given channel.
  ///
  /// @param channel the channel to write to
  /// @return a new message writer that writes to the given channel
  static MessageWriter of(WritableByteChannel channel) {
    return MessageWriterImpl.of(channel, options -> {});
  }

  /// Returns a new message writer with the given options that writes to the given channel.
  ///
  /// @param channel the channel to write to
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message writer with the given options that writes to the given channel
  static MessageWriter of(WritableByteChannel channel, Consumer<OptionBuilder> optionHandler) {
    return MessageWriterImpl.of(channel, optionHandler);
  }

  /// Returns a new message writer that writes to the given output stream.
  ///
  /// @param stream the output stream to write to
  /// @return a new message writer that writes to the given output stream
  static MessageWriter of(OutputStream stream) {
    return MessageWriterImpl.of(stream, options -> {});
  }

  /// Returns a new message writer with the given options that writes to the given output stream.
  ///
  /// @param stream the output stream to write to
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message writer with the given options that writes to the given output stream
  static MessageWriter of(OutputStream stream, Consumer<OptionBuilder> optionHandler) {
    return MessageWriterImpl.of(stream, optionHandler);
  }

  /// Returns a new message writer that writes to the given buffer output.
  ///
  /// @param output the buffer output to write to
  /// @return a new message writer that writes to the given buffer output
  static MessageWriter of(MessageOutput.Buffer output) {
    return MessageWriterImpl.of(output, options -> {});
  }

  /// Returns a new message writer with the given options that writes to the given buffer output.
  ///
  /// @param output the buffer output to write to
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message writer that writes to the given buffer output
  static MessageWriter of(MessageOutput.Buffer output, Consumer<OptionBuilder> optionHandler) {
    return MessageWriterImpl.of(output, optionHandler);
  }

  /// Returns a new message writer that writes to the given sink provider.
  ///
  /// @param provider the sink provider to write to
  /// @return a new message writer that writes to the given sink provider
  static MessageWriter of(MessageSink.Provider provider) {
    return MessageWriterImpl.of(provider, options -> {});
  }

  /// Returns a new message writer with the given options that writes to the given sink provider.
  ///
  /// @param provider the sink provider to write to
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message writer with the given options that writes to the given sink provider
  static MessageWriter of(MessageSink.Provider provider, Consumer<OptionBuilder> optionHandler) {
    return MessageWriterImpl.of(provider, optionHandler);
  }

  /// Returns a new message writer that discards any bytes written.
  ///
  /// @return a new message writer that discards any bytes written
  static MessageWriter ofDiscarding() {
    return MessageWriterImpl.ofDiscarding();
  }

  /// Returns a new message writer with the given underlying [buffer][MessageSink#buffer()]
  /// that discards any bytes written.
  ///
  /// **This method is intended for testing and benchmarking. It should not be used in production code.**
  ///
  /// @param buffer the buffer to use
  /// @return a new message writer with the given underlying [buffer][MessageSink#buffer()]
  ///         that discards any bytes written
  static MessageWriter ofDiscarding(ByteBuffer buffer) {
    return MessageWriterImpl.ofDiscarding(buffer);
  }

  /// Writes a nil (null) value.
  ///
  /// @throws IOException if an I/O error occurs
  void writeNil() throws IOException;

  /// Writes a boolean value.
  ///
  /// @param value the boolean value to write
  /// @throws IOException if an I/O error occurs
  void write(boolean value) throws IOException;

  /// Writes an integer value that fits into `byte`.
  ///
  /// @param value the integer value to write
  /// @throws IOException if an I/O error occurs
  void write(byte value) throws IOException;

  /// Writes an integer value that fits into `short`.
  ///
  /// @param value the integer value to write
  /// @throws IOException if an I/O error occurs
  void write(short value) throws IOException;

  /// Writes an integer value that fits into `int`.
  ///
  /// @param value the integer value to write
  /// @throws IOException if an I/O error occurs
  void write(int value) throws IOException;

  /// Writes an integer value that fits into `long`.
  ///
  /// @param value the integer value to write
  /// @throws IOException if an I/O error occurs
  void write(long value) throws IOException;

  /// Writes a floating point value that fits into `float`.
  ///
  /// @param value the floating point value to write
  /// @throws IOException if an I/O error occurs
  void write(float value) throws IOException;

  /// Writes a floating point value that fits into `double`.
  ///
  /// @param value the floating point value to write
  /// @throws IOException if an I/O error occurs
  void write(double value) throws IOException;

  /// Writes a timestamp value.
  ///
  /// @param value the timestamp value to write
  /// @throws IOException if an I/O error occurs
  void write(Instant value) throws IOException;

  /// Writes a string value.
  ///
  /// To write a string value as a sequence of bytes,
  /// call [#writeStringHeader] followed by [#writePayload].
  ///
  /// @param string the string value to write
  /// @throws IOException if an I/O error occurs
  void write(CharSequence string) throws IOException;

  /// Writes a string value that is used as identifier.
  ///
  /// Calling this method has the same effect as calling [#write(CharSequence)]
  /// except that it indicates to this message writer that it may want to cache the given value.
  ///
  /// @param identifier the identifier to write
  /// @throws IOException if an I/O error occurs
  void writeIdentifier(String identifier) throws IOException;

  /// Encodes and writes a value.
  ///
  /// @param <T> the type of value to encode and write
  /// @param value the value to encode and write
  /// @param encoder the encoder to use
  /// @throws IOException if an I/O error occurs
  <T> void write(T value, MessageEncoder<T> encoder) throws IOException;

  /// Starts writing an array value.
  ///
  /// A call to this method *must* be followed by `elementCount` `writeXxx` calls that write the array's
  /// elements.
  ///
  /// @param elementCount the number of elements in the array
  /// @throws IOException if an I/O error occurs
  void writeArrayHeader(int elementCount) throws IOException;

  /// Starts writing a map value.
  ///
  /// A call to this method *must* be followed by `entryCount * 2` `writeXxx` calls that alternately write
  /// the map's keys and values.
  ///
  /// @param entryCount the number of entries in the map
  /// @throws IOException if an I/O error occurs
  void writeMapHeader(int entryCount) throws IOException;

  /// Starts writing a string value.
  ///
  /// A call to this method *must* be immediately followed by calls to [#writePayload] that
  /// write `length` bytes in total.
  ///
  /// This method is a low-level alternative to [#write(CharSequence)].
  ///
  /// @param length the length, in bytes, of the string value's payload
  /// @throws IOException if an I/O error occurs
  void writeStringHeader(int length) throws IOException;

  /// Starts writing a binary value.
  ///
  /// A call to this method *must* be immediately followed by calls to [#writePayload] that
  /// write `length` bytes in total.
  ///
  /// @param length, the length, in bytes, of the binary value's payload
  /// @throws IOException if an I/O error occurs
  void writeBinaryHeader(int length) throws IOException;

  /// Starts writing an extension value.
  ///
  /// @param length the length, in bytes, of the extension value
  /// @param type the numeric identifier of the extension value's type
  /// @throws IOException if an I/O error occurs
  void writeExtensionHeader(int length, byte type) throws IOException;

  /// Writes the [remaining][ByteBuffer#remaining()] bytes of the given byte buffer.
  ///
  /// Calls to [#writePayload] *must* be preceeded by a call to
  /// [#writeStringHeader], [#writeBinaryHeader], or [#writeExtensionHeader].
  ///
  /// @param buffer the byte buffer to write
  /// @throws IOException if an I/O error occurs
  void writePayload(ByteBuffer buffer) throws IOException;

  /// Writes the [remaining][ByteBuffer#remaining()] bytes of the given byte buffers.
  ///
  /// Calls to [#writePayload] *must* be preceeded by a call to
  /// [#writeStringHeader], [#writeBinaryHeader], or [#writeExtensionHeader].
  ///
  /// @param buffers the byte buffers to write
  /// @throws IOException if an I/O error occurs
  void writePayload(ByteBuffer... buffers) throws IOException;

  /// Writes up to `length` bytes read from the given channel.
  ///
  /// Fewer than `length` bytes may be written if the channel's end of input is reached.
  ///
  /// Calls to [#writePayload] *must* be preceeded by a call to
  /// [#writeStringHeader], [#writeBinaryHeader], or [#writeExtensionHeader].
  ///
  /// @param channel the channel to read from
  /// @param length the number of bytes to write
  /// @return the actual number of bytes written (between 0 and `length`)
  /// @throws IOException if an I/O error occurs
  @SuppressWarnings("UnusedReturnValue")
  long writePayload(ReadableByteChannel channel, long length) throws IOException;

  /// Writes up to `length` bytes read from the given input stream.
  ///
  /// Fewer than `length` bytes may be written if the input stream's end of input is reached.
  ///
  /// Calls to [#writePayload] *must* be preceeded by a call to
  /// [#writeStringHeader], [#writeBinaryHeader], or [#writeExtensionHeader].
  ///
  /// @param stream the input stream to read from
  /// @param length the number of bytes to write
  /// @return the actual number of bytes written (between 0 and `length`)
  /// @throws IOException if an I/O error occurs
  @SuppressWarnings("UnusedReturnValue")
  long writePayload(InputStream stream, long length) throws IOException;

  /// Writes an integer value that fits into a `byte` interpreted as unsigned value.
  ///
  /// @param value the integer value to write
  /// @throws IOException if an I/O error occurs
  void writeUnsigned(byte value) throws IOException;

  /// Writes an integer value that fits into a `short` interpreted as unsigned value.
  ///
  /// @param value the integer value to write
  /// @throws IOException if an I/O error occurs
  void writeUnsigned(short value) throws IOException;

  /// Writes an integer value that fits into an `int` interpreted as unsigned value.
  ///
  /// @param value the integer value to write
  /// @throws IOException if an I/O error occurs
  void writeUnsigned(int value) throws IOException;

  /// Writes an integer value that fits into a `long` interpreted as unsigned value.
  ///
  /// @param value the integer value to write
  /// @throws IOException if an I/O error occurs
  void writeUnsigned(long value) throws IOException;

  /// Flushes this message writer.
  ///
  /// It is not necessary to explicitly flush a writer before [closing][#close()] it.
  ///
  /// If this writer was created with [#of(OutputStream)],
  /// this method calls [OutputStream#flush()].
  /// If this writer was created with [#of(MessageSink.Provider)],
  /// this method calls [MessageSink.Provider#close()].
  ///
  /// @throws IOException if an I/O error occurs
  void flush() throws IOException;

  /// Closes this message writer.
  ///
  /// It is not necessary to explicitly [flush][#flush()] a writer before closing it.
  ///
  /// If this writer was created with [#of(WritableByteChannel)],
  /// this method calls [WritableByteChannel#close()].
  /// If this writer was created with [#of(OutputStream)],
  /// this method calls [OutputStream#close()].
  /// If this writer was created with [#of(MessageSink.Provider)],
  /// this method calls [MessageSink.Provider#close()].
  ///
  /// Subsequent calls to this method have no effect.
  /// **Continued use of a closed message writer can result in data corruption**
  /// and may throw [IllegalStateException].
  ///
  /// @throws IOException if an I/O error occurs
  @Override
  void close() throws IOException;
}
