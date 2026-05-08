package com.hello.android.ui.viewmodel

import app.cash.turbine.test
import com.hello.android.data.CounterRepository
import com.hello.android.domain.Logger
import com.hello.android.domain.model.CounterModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepository: CounterRepository
    private lateinit var mockLogger: Logger
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)

        // Setup default mock behavior
        val initialState = MutableStateFlow(CounterModel(count = 0))
        every { mockRepository.counterState } returns initialState as StateFlow<CounterModel>

        viewModel = MainViewModel(mockRepository, mockLogger)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `increment should call repository increment`() = runTest {
        // Given
        val mutableState = MutableStateFlow(CounterModel(count = 0))
        every { mockRepository.counterState } returns mutableState

        // When
        viewModel.increment()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockRepository.increment() }
    }

    @Test
    fun `reset should call repository reset`() = runTest {
        // Given
        val mutableState = MutableStateFlow(CounterModel(count = 5))
        every { mockRepository.counterState } returns mutableState

        // When
        viewModel.reset()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockRepository.reset() }
    }

    @Test
    fun `counterState should reflect repository state`() = runTest {
        // Given
        val mutableState = MutableStateFlow(CounterModel(count = 42))
        every { mockRepository.counterState } returns mutableState

        // When
        viewModel = MainViewModel(mockRepository, mockLogger)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.counterState.test {
            assertEquals(42, awaitItem().count)
        }
    }
}
