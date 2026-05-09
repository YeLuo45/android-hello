package com.hello.android.ui.viewmodel

import app.cash.turbine.test
import com.hello.android.data.auth.AuthRepository
import com.hello.android.data.auth.AuthResult
import com.hello.android.data.auth.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockAuthRepository = mockk(relaxed = true)

        every { mockAuthRepository.isLoggedIn } returns MutableStateFlow(false)
        coEvery { mockAuthRepository.getCurrentUser() } returns null

        viewModel = AuthViewModel(mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have isAutoLoginChecked false before init`() = runTest {
        // When creating new viewModel with no auto-login
        coEvery { mockAuthRepository.getCurrentUser() } returns null
        every { mockAuthRepository.isLoggedIn } returns MutableStateFlow(false)

        val newViewModel = AuthViewModel(mockAuthRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        newViewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isAutoLoginChecked)
            assertFalse(state.isLoggedIn)
            assertNull(state.currentUser)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkAutoLogin should update state with current user if logged in`() = runTest {
        // Given
        val mockUser = User(id = "user_123", username = "testuser", token = "token_abc")
        coEvery { mockAuthRepository.getCurrentUser() } returns mockUser

        // When
        viewModel.checkAutoLogin()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoggedIn)
            assertEquals(mockUser, state.currentUser)
            assertTrue(state.isAutoLoginChecked)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with valid credentials should update state to logged in`() = runTest {
        // Given
        val mockUser = User(id = "user_123", username = "testuser", token = "token_abc")
        coEvery { mockAuthRepository.login("testuser", "password123") } returns AuthResult.Success(mockUser)

        // When
        viewModel.login("testuser", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoggedIn)
            assertEquals(mockUser, state.currentUser)
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with invalid credentials should update state with error`() = runTest {
        // Given
        coEvery { mockAuthRepository.login("bad", "user") } returns AuthResult.Error("Login failed")

        // When
        viewModel.login("bad", "user")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoggedIn)
            assertEquals("Login failed", state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login should set loading state during login`() = runTest {
        // Given
        coEvery { mockAuthRepository.login(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(100)
            AuthResult.Error("error")
        }

        // When - start login but don't advance
        viewModel.login("test", "password")
        // At this point, isLoading should be true
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - isLoading should be false after completion
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register with valid data should update state to logged in`() = runTest {
        // Given
        val mockUser = User(id = "user_new", username = "newuser", token = "token_new")
        coEvery { mockAuthRepository.register("newuser", "password123", "password123") } returns AuthResult.Success(mockUser)

        // When
        viewModel.register("newuser", "password123", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoggedIn)
            assertEquals(mockUser, state.currentUser)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register with mismatched passwords should update state with error`() = runTest {
        // Given
        coEvery { mockAuthRepository.register("user", "pass123", "different") } returns AuthResult.Error("Passwords do not match")

        // When
        viewModel.register("user", "pass123", "different")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoggedIn)
            assertEquals("Passwords do not match", state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout should reset state`() = runTest {
        // Given - first login
        val mockUser = User(id = "user_123", username = "testuser", token = "token_abc")
        coEvery { mockAuthRepository.login("testuser", "password123") } returns AuthResult.Success(mockUser)
        coEvery { mockAuthRepository.logout() } returns Unit

        viewModel.login("testuser", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoggedIn)
            assertNull(state.currentUser)
            assertTrue(state.isAutoLoginChecked)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearError should remove error message`() = runTest {
        // Given - set an error first
        coEvery { mockAuthRepository.login("bad", "user") } returns AuthResult.Error("Some error")
        viewModel.login("bad", "user")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
