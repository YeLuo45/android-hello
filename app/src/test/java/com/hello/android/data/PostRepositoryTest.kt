package com.hello.android.data

import com.hello.android.data.local.dao.PostDao
import com.hello.android.data.local.entity.PostEntity
import com.hello.android.data.remote.ApiService
import com.hello.android.data.remote.Post
import com.hello.android.domain.Logger
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockPostDao: PostDao
    private lateinit var mockApiService: ApiService
    private lateinit var mockLogger: Logger
    private lateinit var repository: PostRepository

    @Before
    fun setup() {
        mockPostDao = mockk(relaxed = true)
        mockApiService = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)

        repository = PostRepository(
            postDao = mockPostDao,
            apiService = mockApiService,
            logger = mockLogger
        )
    }

    @Test
    fun `getCachedPosts should return mapped posts from DAO`() = runTest {
        // Given
        val entities = listOf(
            PostEntity(id = 1, userId = 1, title = "Title 1", body = "Body 1", cachedAt = 0),
            PostEntity(id = 2, userId = 2, title = "Title 2", body = "Body 2", cachedAt = 0)
        )
        every { mockPostDao.getAllPosts() } returns flowOf(entities)

        // When
        repository.getCachedPosts().collect { posts ->
            // Then
            assertEquals(2, posts.size)
            assertEquals("Title 1", posts[0].title)
            assertEquals("Body 1", posts[0].body)
            assertEquals(1, posts[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshPosts should call API and update database on success`() = runTest {
        // Given
        val apiPosts = listOf(
            Post(id = 1, userId = 1, title = "API Title 1", body = "API Body 1"),
            Post(id = 2, userId = 1, title = "API Title 2", body = "API Body 2")
        )
        coEvery { mockApiService.getPosts() } returns apiPosts
        coEvery { mockPostDao.deleteAll() } just Runs
        coEvery { mockPostDao.insertAll(any()) } just Runs

        // When
        val result = repository.refreshPosts()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        coVerify { mockPostDao.deleteAll() }
        coVerify { mockPostDao.insertAll(any()) }
        verify { mockLogger.log("Posts refreshed: 2 items") }
    }

    @Test
    fun `refreshPosts should return failure on API error`() = runTest {
        // Given
        coEvery { mockApiService.getPosts() } throws Exception("Network error")

        // When
        val result = repository.refreshPosts()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
        verify { mockLogger.log("Failed to refresh posts: Network error") }
    }

    @Test
    fun `getPost should return post by id from DAO`() = runTest {
        // Given
        val entity = PostEntity(id = 5, userId = 1, title = "Specific", body = "Content", cachedAt = 0)
        coEvery { mockPostDao.getPostById(5) } returns entity

        // When
        val result = repository.getPost(5)

        // Then
        assertEquals(5, result?.id)
        assertEquals("Specific", result?.title)
    }

    @Test
    fun `getPost should return null when not found`() = runTest {
        // Given
        coEvery { mockPostDao.getPostById(999) } returns null

        // When
        val result = repository.getPost(999)

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `clearCache should delete all posts from DAO`() = runTest {
        // Given
        coEvery { mockPostDao.deleteAll() } just Runs

        // When
        repository.clearCache()

        // Then
        coVerify { mockPostDao.deleteAll() }
        verify { mockLogger.log("Post cache cleared") }
    }

    @Test
    fun `isCacheValid should return false when cache is empty`() = runTest {
        // Given
        coEvery { mockPostDao.getAllPostsOnce() } returns emptyList()

        // When
        val result = repository.isCacheValid()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isCacheValid should return true when cache is fresh`() = runTest {
        // Given
        val freshTimestamp = System.currentTimeMillis()
        val entities = listOf(
            PostEntity(id = 1, userId = 1, title = "Fresh", body = "Content", cachedAt = freshTimestamp)
        )
        coEvery { mockPostDao.getAllPostsOnce() } returns entities

        // When
        val result = repository.isCacheValid(maxAgeMillis = 15 * 60 * 1000L)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isCacheValid should return false when cache is stale`() = runTest {
        // Given
        val oldTimestamp = System.currentTimeMillis() - (30 * 60 * 1000L) // 30 minutes ago
        val entities = listOf(
            PostEntity(id = 1, userId = 1, title = "Old", body = "Content", cachedAt = oldTimestamp)
        )
        coEvery { mockPostDao.getAllPostsOnce() } returns entities

        // When
        val result = repository.isCacheValid(maxAgeMillis = 15 * 60 * 1000L)

        // Then
        assertFalse(result)
    }
}
