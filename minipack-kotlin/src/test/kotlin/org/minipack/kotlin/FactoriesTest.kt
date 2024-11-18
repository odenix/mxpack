package org.minipack.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class FactoriesTest {
  @Test
  fun `create unpooled allocator`() {
    BufferAllocators.ofUnpooled().use { allocator ->
      val buffer = allocator.pooledByteBuffer(42)
      assertThat(buffer.isDirect).isFalse
      assertThat(buffer.capacity()).isEqualTo(42)
    }
  }

  @Test
  fun `create pooled allocator`() {
    BufferAllocators.ofPooled(useDirectBuffers = true).use { allocator ->
      val buffer = allocator.pooledByteBuffer(42)
      assertThat(buffer.isDirect).isFalse
      assertThat(buffer.capacity()).isGreaterThanOrEqualTo(42)
      allocator.release(buffer)
      val buffer2 = allocator.pooledByteBuffer(42)
      assertThat(buffer2).isSameAs(buffer)
    }
  }

  @Test
  fun `create message reader`() {
    val buffer = ByteBuffer.allocate(1).put(42).flip()
    val source = MessageSources.of(buffer)
    MessageReaders.of(source).use { reader ->
      assertThat(reader.readInt()).isEqualTo(42)
    }
  }

  @Test
  fun `create message writer`() {
    val (sink, output) = MessageSinks.ofBuffer()
    MessageWriters.of(sink).use { writer ->
      writer.write(42)
      writer.flush()
    }
    val buffer = output.get()
    assertThat(buffer.get()).isEqualTo(42)
  }
}
