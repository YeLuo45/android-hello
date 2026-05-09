package com.hello.android.ui.viewmodel

import app.cash.turbine.test
import com.hello.android.analytics.Analytics
import com.hello.android.data.remote.ApiService
import com.hello.android.data.remote.Post
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import androidx.lifecycle.SavedStateHandle

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockApiService: ApiService
    private lateinit var mockSavedStateHandle: SavedStateHandle
    private lateinit var mockAnalytics: Analytics
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockApiService = mockk(relaxed = true)
        mockSavedStateHandle = mockk(relaxed = true)
        mockAnalytics = mockk(relaxed = true)

        every { mockSavedStateHandle.get<HomeUiState>(any()) } returns null

        viewModel = HomeViewModel(
            apiService = mockApiService,
            savedStateHandle = mockSavedStateHandle,
            analytics = mockAnalytics
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty posts and not loading`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList<Post>(), state.posts)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPosts should set loading state then populate posts on success`() = runTest {
        // Given
        val mockPosts = listOf(
            Post(id = 1, userId = 1, title = "Post 1", body = "Body 1"),
            Post(id = 2, userId = 1, title = "Post 2", body = "Body 2")
        )
        coEvery { mockApiService.getPosts() } returns mockPosts

        // When
        viewModel.loadPosts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.posts.size)
            assertEquals("Post 1", state.posts[0].title)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPosts should set error state on failure`() = runTest {
        // Given
        coEvery { mockApiService.getPosts() } throws Exception("Network error")

        // When
        viewModel.loadPosts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList<Post>(), state.posts)
            assertEquals(false, state.isLoading)
            assertEquals("Network error", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPosts should not trigger another load while already loading`() = runTest {
        // Given
        coEvery { mockApiService.getPosts() } coAnswers {
            kotlinx.coroutines.delay(1000)
            listOf(Post(1, 1, "Title", "Body"))
        }

        // When - trigger twice quickly
        viewModel.loadPosts()
        viewModel.loadPosts() // Should be ignored since already loading
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should only call getPosts once
        coVerify(exactly = 1) { mockApiService.getPosts() }
    }

    @Test
    fun `loadPosts should track analytics on success`() = runTest {
        // Given
        val mockPosts = listOf(
            Post(id = 1, userId = 1, title = "Post 1", body = "Body 1"),
            Post(id = 2, userId = 1, title = "Post 2", body = "Body 2"),
            Post(id = 3, userId = 1, title = "Post 3", body = "Body 3")
        )
        coEvery { mockApiService.getPosts() } returns mockPosts

        // When
        viewModel.loadPosts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockAnalytics.track("posts_loaded", mapOf("count" to 3)) }
    }

    @Test
    fun `init should track screen_view analytics`() = runTest {
        // Then
        coVerify { mockAnalytics.track("screen_view", mapOf("screen" to "HomeScreen")) }
    }
}
