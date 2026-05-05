package com.hello.android.data

import com.hello.android.domain.Logger
import com.hello.android.domain.model.CounterModel
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CounterRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockLogger: Logger
    private lateinit var repository: CounterRepository

    @Before
    fun setup() {
        mockLogger = mockk(relaxed = true)
        repository = CounterRepository(mockLogger)
    }

    @Test
    fun `initial count should be zero`() = runTest {
        repository.counterState.test {
            assertEquals(0, awaitItem().count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `increment should increase count by one`() = runTest {
        repository.increment()
        advanceUntilIdle()
        verify { mockLogger.log("Counter incremented to: 1") }
    }

    @Test
    fun `reset should set count back to zero`() = runTest {
        repository.increment()
        repository.increment()
        advanceUntilIdle()
        repository.reset()
        advanceUntilIdle()
        verify { mockLogger.log("Counter reset to 0") }
    }

    @Test
    fun `multiple increments should accumulate correctly`() = runTest {
        repeat(5) { repository.increment() }
        advanceUntilIdle()
        repository.counterState.test {
            assertEquals(5, awaitItem().count)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
