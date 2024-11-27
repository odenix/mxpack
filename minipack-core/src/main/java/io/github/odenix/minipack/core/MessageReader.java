/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.odenix.minipack.core;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;
import java.util.function.Consumer;

import io.github.odenix.minipack.core.internal.MessageReaderImpl;

/// Reads values encoded in MessagePack from a channel, input stream, byte buffer, or [MessageSink.Provider].
///
/// To create a new message reader, call [MessageReader#of].
/// To read values, call the various `readXxx` methods.
/// To determine the next value's type, call [#nextType()].
/// To close a message reader, call [#close()].
/// **Continued use of a closed message reader can result in data corruption.**
///
/// For usage examples, see [Reading data](https://odenix.github.io/minipack/examples/#reading-data).
public sealed interface MessageReader extends Closeable permits MessageReaderImpl {
  /// A builder of message reader options.
  sealed interface OptionBuilder permits MessageReaderImpl.OptionBuilderImpl {
    /// Sets the buffer allocator to be used by the message writer.
    ///
    /// Default: [BufferAllocator#ofUnpooled()]
    ///
    /// @param allocator the buffer allocator to be used by the message writer
    /// @return this builder
    OptionBuilder allocator(BufferAllocator allocator);

    /// Sets the capacity of the message reader's [read buffer][MessageSink#buffer()].
    ///
    /// This option may affect I/O read performance.
    ///
    /// Default: `1024 * 8`
    ///
    /// @param capacity the capacity of the message reader's read buffer
    /// @return this builder
    @SuppressWarnings("UnusedReturnValue")
    OptionBuilder readBufferCapacity(int capacity);

    /// Sets the string decoder to be used by [MessageReader#readString()].
    ///
    /// Default: [MessageDecoder#ofStrings()]
    ///
    /// @param decoder the string decoder to be used by [MessageReader#readString()]
    /// @return this builder
    OptionBuilder stringDecoder(MessageDecoder<String> decoder);

    /// Sets the string decoder to be used by [MessageReader#readIdentifier()].
    ///
    /// Default: [MessageDecoder#ofStrings()]
    ///
    /// @param decoder the string decoder to be used by [MessageReader#readIdentifier()]
    /// @return this builder
    OptionBuilder identifierDecoder(MessageDecoder<String> decoder);
  }

  /// Returns a new message reader that reads from the given channel.
  ///
  /// @param channel the channel to read from
  /// @return a new message reader that reads from the given channel
  static MessageReader of(ReadableByteChannel channel) {
    return MessageReaderImpl.of(channel, options -> {});
  }

  /// Returns a new message reader with the given options that reads from the given channel.
  ///
  /// @param channel the channel to read from
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message reader with the given options that reads from the given channel
  static MessageReader of(ReadableByteChannel channel, Consumer<OptionBuilder> optionHandler) {
    return MessageReaderImpl.of(channel, optionHandler);
  }

  /// Returns a new message reader that reads from the given input stream.
  ///
  /// @param stream the input stream to read from
  /// @return a new message reader that reads from the given input stream
  static MessageReader of(InputStream stream) {
    return MessageReaderImpl.of(stream, options -> {});
  }

  /// Returns a new message reader with the given options that reads from the given input stream.
  ///
  /// @param stream the input stream to read from
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message reader with the given options that reads from the given input stream
  static MessageReader of(InputStream stream, Consumer<OptionBuilder> optionHandler) {
    return MessageReaderImpl.of(stream, optionHandler);
  }

  /// Returns a new message reader that reads from the given byte buffer.
  ///
  /// @param buffer the byte buffer to read from
  /// @return a new message reader that reads from the given byte buffer
  static MessageReader of(LeasedByteBuffer buffer) {
    return MessageReaderImpl.of(buffer, options -> {});
  }

  /// Returns a new message reader with the given options that reads from the given byte buffer.
  ///
  /// @param buffer the byte buffer to read from
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message reader with the given options that reads from the given byte buffer
  static MessageReader of(LeasedByteBuffer buffer, Consumer<OptionBuilder> optionHandler) {
    return MessageReaderImpl.of(buffer, optionHandler);
  }

  /// Returns a new message reader that reads from the given byte buffer.
  ///
  /// @param buffer the byte buffer to read from
  /// @return a new message reader that reads from the given byte buffer
  static MessageReader of(ByteBuffer buffer) {
    return MessageReaderImpl.of(buffer, options -> {});
  }

  /// Returns a new message reader with the given options that reads from the given byte buffer.
  ///
  /// @param buffer the byte buffer to read from
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message reader with the given options that reads from the given byte buffer
  static MessageReader of(ByteBuffer buffer, Consumer<OptionBuilder> optionHandler) {
    return MessageReaderImpl.of(buffer, optionHandler);
  }

  /// Returns a new message reader that reads from the given source provider.
  ///
  /// @param provider the source provider to read from
  /// @return a new message reader that reads from the given source provider
  static MessageReader of (MessageSource.Provider provider) {
    return MessageReaderImpl.of(provider, options -> {});
  }

  /// Returns a new message reader with the given options that reads from the given source provider.
  ///
  /// @param provider the source provider to read from
  /// @param optionHandler a handler that receives an [OptionBuilder]
  /// @return a new message reader with the given options that reads from the given source provider
  static MessageReader of (MessageSource.Provider provider, Consumer<OptionBuilder> optionHandler) {
    return MessageReaderImpl.of(provider, optionHandler);
  }

  /// Returns a new message reader that will throw [EOFException] when a value is read.
  ///
  /// @return a new message reader that will throw [EOFException] when a value is read
  static MessageReader ofEmpty() {
    return MessageReaderImpl.ofEmpty();
  }

  /// Reads the next value's type without consuming it.
  ///
  /// Consecutive calls to this method will return the same result.
  ///
  /// @return the next value's type
  /// @throws EOFException if the end of input is reached before the type has been read
  /// @throws IOException if an I/O error occurs
  MessageType nextType() throws IOException;

  /// Skips the next value.
  ///
  /// @throws EOFException if the end of input is reached before the value has been skipped
  /// @throws IOException if an I/O error occurs
  void skipValue() throws IOException;

  /// Skips the next `count` values.
  ///
  /// @param count the number of values to skip
  /// @throws EOFException if the end of input is reached before the values have been skipped
  /// @throws IOException if an I/O error occurs
  void skipValue(int count) throws IOException;

  /// Reads a nil (null) value.
  ///
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  void readNil() throws IOException;

  /// Reads a boolean value.
  ///
  /// @return the boolean value read
  /// @throws MiniPackException.TypeMismatch if the value read is not a boolean value
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  boolean readBoolean() throws IOException;

  /// Reads an integer value that fits into `byte`.
  ///
  /// @return the integer value read
  /// @throws MiniPackException.TypeMismatch if the value read is not an integer value or does not fit into `byte`
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  byte readByte() throws IOException;

  /// Reads an integer value that fits into `short`.
  ///
  /// @return the integer value read
  /// @throws MiniPackException.TypeMismatch if the value read is not an integer value or does not fit into `short`
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  short readShort() throws IOException;

  /// Reads an integer value that fits into `int`.
  ///
  /// @return the integer value read
  /// @throws MiniPackException.TypeMismatch if the value read is not an integer value or does not fit into `int`
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  int readInt() throws IOException;

  /// Reads an integer value.
  ///
  /// @return the integer value read
  /// @throws MiniPackException.TypeMismatch if the value read is not an integer value
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  long readLong() throws IOException;

  /// Reads a floating point value that fits into `float`.
  ///
  /// @return the floating point value read
  /// @throws MiniPackException.TypeMismatch if the value read is not a floating point value or does not fit into `float`
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  float readFloat() throws IOException;

  /// Reads a floating point value.
  ///
  /// @return the floating point value read
  /// @throws MiniPackException.TypeMismatch if the value read is not a floating point value
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  double readDouble() throws IOException;

  /// Reads a timestamp value.
  ///
  /// @return the timestamp value read
  /// @throws MiniPackException.TypeMismatch if the value read is not a timestamp value
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  Instant readTimestamp() throws IOException;

  /// Reads a string value.
  ///
  /// To read a string value as a sequence of bytes, call [#readStringHeader()] followed by
  /// [#readPayload].
  ///
  /// @return the string value read
  /// @throws MiniPackException.TypeMismatch if the value read is not a string value
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  String readString() throws IOException;

  /// Reads a string value that is used as identifier.
  ///
  /// Calling this method has the same effect as calling [#readString()]
  /// except that it indicates to this message reader that it may want to cache the returned value.
  ///
  /// @return the identifier read
  /// @throws MiniPackException.TypeMismatch if the read value is not a string value
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  String readIdentifier() throws IOException;

  /// Reads and decodes a value.
  ///
  /// @param <T> the type of value to decode
  /// @param decoder the decoder to use
  /// @return the read and decoded value
  /// @throws MiniPackException.TypeMismatch if the value read cannot be decoded with the given decoder
  /// @throws EOFException if the end of input is reached before the value has been read
  /// @throws IOException if an I/O error occurs
  <T> T read(MessageDecoder<T> decoder) throws IOException;

  /// Starts reading an array value.
  ///
  /// A call to this method, returning `n`, *must* be followed by `n` `readXxx` calls that read the array's
  /// elements.
  ///
  /// @return the number of elements in the array
  /// @throws IOException if an I/O error occurs
  int readArrayHeader() throws IOException;

  /// Starts reading a map value.
  ///
  /// A call to this method, returning `n`, *must* be followed
  /// by `n * 2` `readXxx` calls that alternately read the map's keys and values.
  ///
  /// @return the number of entries in the map
  /// @throws IOException if an I/O error occurs
  int readMapHeader() throws IOException;

  /// Starts reading a binary value.
  ///
  /// A call to this method, returning `n`, *must* be immediately followed
  /// by calls to [#readPayload] that read `n` bytes in total.
  ///
  /// @return the length, in bytes, of the binary value
  /// @throws IOException if an I/O error occurs
  int readBinaryHeader() throws IOException;

  /// Starts reading a string value as a sequence of bytes.
  ///
  /// A call to this method, returning `n`, *must* be immediately followed
  /// by calls to [#readPayload] that read `n` bytes in total.
  ///
  /// This method is a low-level alternative to [#readString()].
  ///
  /// @return the length, in bytes, of the string
  /// @throws IOException if an I/O error occurs
  int readStringHeader() throws IOException;

  /// Starts reading an extension value.
  ///
  /// A call to this method, returning an extension header with length `n`,
  /// *must* be immediately followed by calls to [#readPayload] that read `n` bytes in total.
  ///
  /// @return the header of the extension value
  /// @throws IOException if an I/O error occurs
  ExtensionHeader readExtensionHeader() throws IOException;

  /// Reads up to `length` bytes into the given channel.
  ///
  /// Fewer than `length` bytes may be read if this reader reaches the end of input.
  ///
  /// @param channel the channel to write to
  /// @param length the number of bytes to read
  /// @return the actual number of bytes read
  /// @throws IOException if an I/O error occurs
  @SuppressWarnings("UnusedReturnValue")
  long readPayload(WritableByteChannel channel, long length) throws IOException;

  /// Reads up to `length` bytes into the given output stream.
  ///
  /// Fewer than `length` bytes may be read if this reader reaches the end of input.
  ///
  /// @param stream the output stream to write to
  /// @param length the number of bytes to read
  /// @return the actual number of bytes read
  /// @throws IOException if an I/O error occurs
  /// @throws IOException if an I/O error occurs
  @SuppressWarnings("UnusedReturnValue")
  long readPayload(OutputStream stream, long length) throws IOException;

  /// Reads up to [ByteBuffer#remaining()] bytes into the given byte buffer.
  ///
  /// Fewer than [ByteBuffer#remaining()] bytes may be read if this reader reaches the end of input.
  ///
  /// @param buffer the byte buffer to read into
  /// @throws IOException if an I/O error occurs
  void readPayload(ByteBuffer buffer) throws IOException;

  /// Reads an integer value that fits into `byte` interpreted as unsigned value.
  ///
  /// @return the integer value read
  /// @throws IOException if an I/O error occurs
  byte readUByte() throws IOException;

  /// Reads an integer value that fits into `short` interpreted as unsigned value.
  ///
  /// @return the integer value read
  /// @throws IOException if an I/O error occurs
  short readUShort() throws IOException;

  /// Reads an integer value that fits into `int` interpreted as unsigned value.
  ///
  /// @return the integer value read
  /// @throws IOException if an I/O error occurs
  int readUInt() throws IOException;

  /// Reads an integer value that fits into a `long` interpreted as unsigned value.
  ///
  /// @return the integer value read
  /// @throws IOException if an I/O error occurs
  long readULong() throws IOException;

  /// Closes this message reader.
  ///
  /// If this reader was created with [#of(ReadableByteChannel)],
  /// this method calls [ReadableByteChannel#close()].
  /// If this reader was created with [#of(InputStream)],
  /// this method calls [InputStream#close()].
  /// If this reader was created with [#of(LeasedByteBuffer)],
  /// this method calls [LeasedByteBuffer#close()].
  /// If this reader was created with [#of(MessageSource.Provider)],
  /// this method calls [MessageSource.Provider#close()].
  ///
  /// Subsequent calls to this method have no effect.
  /// **Continued use of a closed message reader can result in data corruption**
  /// and may throw [IllegalStateException].
  ///
  /// @throws IOException if an I/O error occurs
  @Override
  void close() throws IOException;
}
