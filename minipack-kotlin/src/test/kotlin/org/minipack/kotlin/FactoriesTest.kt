/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.kotlin

import java.nio.ByteBuffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FactoriesTest {
  @Test
  fun `create unpooled allocator`() {
    BufferAllocators.ofUnpooled().use { allocator ->
      val buffer = allocator.getByteBuffer(42).get()
      assertThat(buffer.isDirect).isFalse
      assertThat(buffer.capacity()).isEqualTo(42)
    }
  }

  @Test
  fun `create pooled allocator`() {
    BufferAllocators.ofPooled().use { allocator ->
      val leasedBuffer = allocator.getByteBuffer(42)
      val buffer = leasedBuffer.get()
      assertThat(buffer.isDirect).isFalse()
      assertThat(buffer.capacity()).isGreaterThanOrEqualTo(42)
      leasedBuffer.close()
      val leasedBuffer2 = allocator.getByteBuffer(42)
      val buffer2 = leasedBuffer2.get()
      assertThat(leasedBuffer2).isNotSameAs(leasedBuffer)
      assertThat(buffer2).isSameAs(buffer)
    }
  }

  @Test
  fun `create message reader`() {
    val buffer = ByteBuffer.allocate(1).put(42).flip()
    MessageReaders.of(buffer).use { reader -> assertThat(reader.readInt()).isEqualTo(42) }
  }

  @Test
  fun `create message writer`() {
    val output = BufferOutputs.of()
    MessageWriters.of(output).use { writer -> writer.write(42) }
    val leasedBuffer = output.get()
    val buffer = leasedBuffer.get()
    assertThat(buffer.get()).isEqualTo(42)
  }

  @Test
  fun `create string encoder`() {
    val encoder = MessageEncoders.ofStrings()
    assertThat(encoder).isNotNull
  }

  @Test
  fun `create string decoder`() {
    val decoder = MessageDecoders.ofStrings()
    assertThat(decoder).isNotNull
  }
}
