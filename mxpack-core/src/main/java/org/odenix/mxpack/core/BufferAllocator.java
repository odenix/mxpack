/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.function.Consumer;

import org.odenix.mxpack.core.internal.AbstractBufferAllocator;
import org.odenix.mxpack.core.internal.PooledBufferAllocator;
import org.odenix.mxpack.core.internal.UnpooledBufferAllocator;

/// An allocator of byte and char buffers.
///
/// By default, MxPack allocates buffers with an [unpooled][#ofUnpooled] 
/// allocator with default [options][UnpooledOptionBuilder].
/// To use a different allocator,
/// set [MessageReader.OptionBuilder#allocator] when creating a [MessageReader],
/// or [MessageWriter.OptionBuilder#allocator] when creating a [MessageWriter].
///
/// If frequent buffer allocations are not a concern, use an [unpooled][#ofUnpooled()] allocator.
/// To reduce buffer allocations, use a single [pooled][#ofPooled()] allocator
/// for all message sources and sinks. Allocators can be safely shared by multiple threads.
/// Only pooled allocators can allocate [direct][PooledOptionBuilder#preferDirectBuffers] byte buffers.
///
/// To create an allocator, call [#ofUnpooled] or [#ofPooled].
/// Because unpooled and pooled allocators implement the same `BufferAllocator` interface,
/// switching between them is easy.
/// To obtain a buffer, call [#getByteBuffer] or [#getCharBuffer].
/// When a buffer is no longer used, call [LeasedByteBuffer#close()] or [LeasedCharBuffer#close()].
/// Failing to close an unused buffer reduces the efficiency of buffer pooling.
///
/// When an allocator is no longer used, call [#close] to free its buffer pool.
///
/// For usage examples, see [Buffer allocation](https://odenix.org/mxpack/examples/#buffer-allocation).
public sealed interface BufferAllocator extends Closeable
    permits AbstractBufferAllocator, UnpooledBufferAllocator, PooledBufferAllocator {
  /// Options for creating [unpooled][#ofUnpooled()] buffer allocators.
  sealed interface UnpooledOptionBuilder permits AbstractBufferAllocator.OptionBuilderImpl {
    /// Sets the maximum capacity, in bytes, that may be requested by [#getByteBuffer].
    ///
    /// If [#getByteBuffer] is called with an argument greater than `capacity`,
    /// [MxPackException.SizeLimitExceeded] is thrown.
    ///
    /// Default: [Integer#MAX_VALUE]
    ///
    /// @param capacity the maximum capacity, in bytes, that may be requested by [#getByteBuffer]
    /// @return this builder
    UnpooledOptionBuilder maxByteBufferCapacity(int capacity);

    /// Sets the maximum capacity, in chars, that may be requested by [#getCharBuffer].
    ///
    /// If [#getCharBuffer] is called with an argument greater than `capacity`,
    /// [MxPackException.SizeLimitExceeded] is thrown.
    ///
    /// Default: [Integer#MAX_VALUE]
    ///
    /// @param capacity the maximum capacity, in chars, that may be requested by [#getCharBuffer]
    /// @return this builder
    UnpooledOptionBuilder maxCharBufferCapacity(int capacity);
  }

  /// Options for creating [pooled][#ofPooled()] buffer allocators.
  sealed interface PooledOptionBuilder permits AbstractBufferAllocator.OptionBuilderImpl {
    /// Sets the maximum capacity, in bytes, that may be requested by [#getByteBuffer].
    ///
    /// If [#getByteBuffer] is called with an argument greater than `capacity`,
    /// [MxPackException.SizeLimitExceeded] is thrown.
    ///
    /// Default: [Integer#MAX_VALUE]
    ///
    /// @param capacity the maximum capacity, in bytes, that may be requested by [#getByteBuffer]
    /// @return this builder
    PooledOptionBuilder maxByteBufferCapacity(int capacity);

    /// Sets the maximum capacity, in chars, that may be requested by [#getCharBuffer].
    ///
    /// If [#getCharBuffer] is called with an argument greater than `capacity`,
    /// [MxPackException.SizeLimitExceeded] is thrown.
    ///
    /// Default: [Integer#MAX_VALUE]
    ///
    /// @param capacity the maximum capacity, in chars, that may be requested by [#getCharBuffer]
    /// @return this builder
    PooledOptionBuilder maxCharBufferCapacity(int capacity);

    /// Sets the maximum capacity, in bytes, of a pooled byte buffer.
    ///
    /// If [#getByteBuffer] is called with an argument greater than `capacity`,
    /// an unpooled heap buffer is returned.
    ///
    /// Default: `1024 * 1024`
    ///
    /// @param capacity the maximum capacity, in bytes, of a pooled byte buffer
    /// @return this builder
    PooledOptionBuilder maxPooledByteBufferCapacity(int capacity);

    /// Sets the maximum capacity, in chars, of a pooled char buffer.
    ///
    /// If [#getCharBuffer] is called with an argument greater than `capacity`,
    /// an unpooled buffer is returned.
    ///
    /// Default: `1024 * 512`
    ///
    /// @param capacity the maximum capacity, in chars, of a pooled char buffer
    /// @return this builder
    PooledOptionBuilder maxPooledCharBufferCapacity(int capacity);

    /// Sets the maximum capacity of the byte buffer pool.
    ///
    /// If the byte buffer pool runs out of buffers after the maximum pool capacity has been reached,
    /// additional unpooled byte buffers are allocated on the heap.
    ///
    /// Default: `1024 * 1024 * 64`
    ///
    /// @param capacity the maximum capacity of the byte buffer pool
    /// @return this builder
    PooledOptionBuilder maxByteBufferPoolCapacity(int capacity);

    /// Sets the maximum capacity of the char buffer pool.
    ///
    /// If the char buffer pool runs out of buffers after the maximum pool capacity has been reached,
    /// additional unpooled char buffers are allocated.
    ///
    /// Default: `1024 * 1024 * 32`
    ///
    /// @param capacity the maximum capacity of the char buffer pool
    /// @return this builder
    PooledOptionBuilder maxCharBufferPoolCapacity(int capacity);

    /// Sets whether to preferably allocate [direct][ByteBuffer#isDirect()] byte buffers.
    ///
    /// If set to `true`, byte buffers are preferably allocated with [ByteBuffer#allocateDirect].
    /// If set to `false`, byte buffers are allocated with [ByteBuffer#allocate].
    ///
    /// If the byte buffer pool runs out of buffers after the
    /// [maximum pool capacity][PooledOptionBuilder#maxByteBufferPoolCapacity] has been reached,
    /// additional unpooled buffers are allocated with [ByteBuffer#allocate] even if this option is set to `true`.
    ///
    /// This option does not apply to char buffers,
    /// which are always allocated with [CharBuffer#allocate].
    ///
    /// Default: `false`
    ///
    /// @param flag whether to preferably allocate direct byte buffers
    /// @return this builder`
    PooledOptionBuilder preferDirectBuffers(boolean flag);
  }

  /// Returns a new buffer allocator that allocates
  /// a new buffer each time [#getByteBuffer] or [#getCharBuffer] is called.
  ///
  /// @return a new buffer allocator that allocates a new buffer
  ///         each time [#getByteBuffer] or [#getCharBuffer] is called
  static BufferAllocator ofUnpooled() {
    return UnpooledBufferAllocator.of(options -> {});
  }

  /// Returns a new buffer allocator with the given options that allocates
  /// a new buffer each time [#getByteBuffer] or [#getCharBuffer] is called.
  ///
  /// @param optionHandler a handler that receives an [UnpooledOptionBuilder]
  /// @return a new buffer allocator with the given options
  ///         that allocates a new buffer each time [#getByteBuffer] or [#getCharBuffer] is called
  static BufferAllocator ofUnpooled(Consumer<UnpooledOptionBuilder> optionHandler) {
    return UnpooledBufferAllocator.of(optionHandler);
  }

  /// Returns a new buffer allocator that maintains a buffer pool to reduce buffer allocations.
  ///
  /// This allocator supports the use of [direct][PooledOptionBuilder#preferDirectBuffers] buffers.
  ///
  /// The current pooled allocator implementation behaves as follows:
  /// * The buffer pool is grown as needed until
  /// the maximum pool capacity for [byte][PooledOptionBuilder#maxByteBufferPoolCapacity]
  /// and [char][PooledOptionBuilder#maxCharBufferPoolCapacity] buffers is reached.
  /// * If the buffer pool runs out of buffers after the maximum pool capacity has been reached,
  ///   additional unpooled buffers are allocated.
  /// * The buffer pool is never shrunk. It is freed when the allocator is [closed][BufferAllocator#close()].
  ///
  /// @return a new buffer allocator that maintains a buffer pool to reduce buffer allocations
  static BufferAllocator ofPooled() {
    return PooledBufferAllocator.of(options -> {});
  }

  /// Returns a new buffer allocator with the given options
  /// that maintains a buffer pool to reduce buffer allocations.
  ///
  /// This allocator supports the use of [direct][PooledOptionBuilder#preferDirectBuffers] buffers.
  ///
  /// The current pooled allocator implementation behaves as follows:
  /// * The buffer pool is grown as needed until
  /// the maximum pool capacity for [byte][PooledOptionBuilder#maxByteBufferPoolCapacity]
  /// and [char][PooledOptionBuilder#maxCharBufferPoolCapacity] buffers is reached.
  /// * If the buffer pool runs out of buffers after the maximum pool capacity has been reached,
  ///   additional unpooled buffers are allocated.
  /// * The buffer pool is never shrunk. It is freed when the allocator is [closed][BufferAllocator#close()].
  ///
  /// @param optionHandler a handler that receives a [PooledOptionBuilder]
  /// @return a new buffer allocator with the given options that maintains a buffer pool
  ///         to reduce buffer allocations
  static BufferAllocator ofPooled(Consumer<PooledOptionBuilder> optionHandler) {
    return PooledBufferAllocator.of(optionHandler);
  }

  /// Returns the maximum buffer capacity, in bytes, that may be requested by [#getByteBuffer].
  ///
  /// The returned value can be set with [UnpooledOptionBuilder#maxByteBufferCapacity]
  /// and [PooledOptionBuilder#maxByteBufferCapacity].
  /// 
  /// Default: [Integer#MAX_VALUE]
  /// 
  /// @return the maximum buffer capacity, in bytes, that may be requested by [#getByteBuffer]
  int maxByteBufferCapacity();

  /// Returns the maximum buffer capacity, in chars, that may be requested by [#getCharBuffer].
  ///
  /// The returned value can be set with [UnpooledOptionBuilder#maxCharBufferCapacity]
  /// and [PooledOptionBuilder#maxCharBufferCapacity].
  ///
  /// Default: [Integer#MAX_VALUE]
  /// 
  /// @return the maximum buffer capacity, in chars, that may be requested by [#getCharBuffer]
  int maxCharBufferCapacity();

  /// Returns a leased [ByteBuffer] with at least the given [capacity][ByteBuffer#capacity].
  ///
  /// The returned buffer is [cleared][ByteBuffer#clear()].
  /// When the buffer is no longer used, it should be [closed][LeasedByteBuffer#close()].
  /// Failing to close an unused buffer reduces the efficiency of buffer [pooling][#ofPooled].
  ///
  /// @param capacity the minimum capacity, in bytes, of the leased buffer
  /// @return a leased [ByteBuffer] with at least the given [capacity][ByteBuffer#capacity]
  /// @throws MxPackException.SizeLimitExceeded if the given capacity
  ///         is greater than the maximum capacity
  /// @throws IllegalStateException if this allocator has already been closed
  LeasedByteBuffer getByteBuffer(int capacity);

  /// Returns a leased [CharBuffer] with at least the given [capacity][ByteBuffer#capacity].
  ///
  /// The returned buffer is [cleared][ByteBuffer#clear()].
  /// When the buffer is no longer used, it should be [closed][LeasedByteBuffer#close()].
  /// Failing to close an unused buffer reduces the efficiency of buffer [pooling][#ofPooled].
  ///
  /// @param capacity the minimum capacity, in chars, of the leased buffer
  /// @return a leased [CharBuffer] with at least the given [capacity][ByteBuffer#capacity]
  /// @throws MxPackException.SizeLimitExceeded if the given capacity
  ///         is greater than the maximum capacity
  /// @throws IllegalStateException if this allocator has already been closed
  LeasedCharBuffer getCharBuffer(int capacity);

  /// Closes this buffer allocator, freeing any pooled buffers.
  ///
  /// Subsequent calls to this method have no effect.
  /// Subsequent calls to [#getByteBuffer] or [#getCharBuffer] throw [IllegalStateException].
  void close();
}
