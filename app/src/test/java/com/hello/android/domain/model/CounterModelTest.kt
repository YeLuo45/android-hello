package com.hello.android.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CounterModelTest {

    @Test
    fun `default count should be zero`() {
        val model = CounterModel()
        assertEquals(0, model.count)
    }

    @Test
    fun `count can be set to arbitrary value`() {
        val model = CounterModel(count = 42)
        assertEquals(42, model.count)
    }

    @Test
    fun `counter model data class equality works correctly`() {
        val model1 = CounterModel(count = 10)
        val model2 = CounterModel(count = 10)
        val model3 = CounterModel(count = 20)

        assertEquals(model1, model2)
        assertEquals(model1.hashCode(), model2.hashCode())
        assert(model1 != model3)
    }

    @Test
    fun `counter model copy works correctly`() {
        val original = CounterModel(count = 5)
        val copied = original.copy(count = 10)

        assertEquals(5, original.count)
        assertEquals(10, copied.count)
    }

    @Test
    fun `toString contains count value`() {
        val model = CounterModel(count = 99)
        assert(toString().contains("count"))
    }
}
