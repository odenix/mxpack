package org.minipack.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class FactoriesTest {
  @Test
  fun `create unpooled allocator`() {
    BufferAllocators.ofUnpooled().use { allocator ->
      val buffer = allocator.getByteBuffer(42).value()
      assertThat(buffer.isDirect).isFalse
      assertThat(buffer.capacity()).isEqualTo(42)
    }
  }

  @Test
  fun `create pooled allocator`() {
    BufferAllocators.ofPooled().use { allocator ->
      val pooled = allocator.getByteBuffer(42)
      val buffer = pooled.value()
      assertThat(buffer.isDirect).isFalse()
      assertThat(buffer.capacity()).isGreaterThanOrEqualTo(42)
      pooled.close()
      val pooled2 = allocator.getByteBuffer(42)
      val buffer2 = pooled2.value()
      assertThat(pooled2).isNotSameAs(pooled)
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
    val buffer = output.get().value()
    assertThat(buffer.get()).isEqualTo(42)
  }
}
