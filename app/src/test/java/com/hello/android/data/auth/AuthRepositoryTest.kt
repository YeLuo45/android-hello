package com.hello.android.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockContext: Context
    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var repository: AuthRepository

    private val mockPreferences = mutableMapOf<String, String>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(preferencesDataStore)
        mockDataStore = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        every { mockContext.authDataStore } returns mockDataStore

        coEvery { mockDataStore.data } returns flowOf(mockPreferences)
    }

    private fun createRepository(): AuthRepository {
        return AuthRepository(mockContext)
    }

    @Test
    fun `login with blank username should return error`() = runTest {
        repository = createRepository()

        val result = repository.login("", "password123")

        assertTrue(result is AuthResult.Error)
        assertEquals("Username and password cannot be empty", (result as AuthResult.Error).message)
    }

    @Test
    fun `login with blank password should return error`() = runTest {
        repository = createRepository()

        val result = repository.login("username", "")

        assertTrue(result is AuthResult.Error)
        assertEquals("Username and password cannot be empty", (result as AuthResult.Error).message)
    }

    @Test
    fun `login with short password should return error`() = runTest {
        repository = createRepository()

        val result = repository.login("username", "12345")

        assertTrue(result is AuthResult.Error)
        assertEquals("Password must be at least 6 characters", (result as AuthResult.Error).message)
    }

    @Test
    fun `login with valid credentials should return success and save data`() = runTest {
        repository = createRepository()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            val transformation = arg<(Preferences) -> Unit>(0)
            transformation(mockPreferences as Preferences)
            kotlinx.coroutines.unit
        }

        val result = repository.login("testuser", "password123")

        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).user
        assertEquals("testuser", user.username)
        assertNotNull(user.token)
        assertTrue(user.token.startsWith("mock_token_"))
    }

    @Test
    fun `register with blank username should return error`() = runTest {
        repository = createRepository()

        val result = repository.register("", "password123", "password123")

        assertTrue(result is AuthResult.Error)
        assertEquals("Username cannot be empty", (result as AuthResult.Error).message)
    }

    @Test
    fun `register with short username should return error`() = runTest {
        repository = createRepository()

        val result = repository.register("ab", "password123", "password123")

        assertTrue(result is AuthResult.Error)
        assertEquals("Username must be at least 3 characters", (result as AuthResult.Error).message)
    }

    @Test
    fun `register with short password should return error`() = runTest {
        repository = createRepository()

        val result = repository.register("username", "12345", "12345")

        assertTrue(result is AuthResult.Error)
        assertEquals("Password must be at least 6 characters", (result as AuthResult.Error).message)
    }

    @Test
    fun `register with mismatched passwords should return error`() = runTest {
        repository = createRepository()

        val result = repository.register("username", "password123", "different")

        assertTrue(result is AuthResult.Error)
        assertEquals("Passwords do not match", (result as AuthResult.Error).message)
    }

    @Test
    fun `register with valid data should return success`() = runTest {
        repository = createRepository()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            val transformation = arg<(Preferences) -> Unit>(0)
            transformation(mockPreferences as Preferences)
            kotlinx.coroutines.unit
        }

        val result = repository.register("newuser", "password123", "password123")

        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).user
        assertEquals("newuser", user.username)
    }

    @Test
    fun `logout should clear all auth data`() = runTest {
        repository = createRepository()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            val transformation = arg<(Preferences) -> Unit>(0)
            transformation(mockPreferences as Preferences)
            kotlinx.coroutines.unit
        }

        repository.logout()

        verify { mockDataStore.edit(any()) }
    }

    @Test
    fun `getCurrentUser when not logged in should return null`() = runTest {
        repository = createRepository()
        mockPreferences.clear()

        val result = repository.getCurrentUser()

        assertNull(result)
    }

    @Test
    fun `getCurrentUser when logged in should return user`() = runTest {
        repository = createRepository()
        mockPreferences["auth_token"] = "test_token"
        mockPreferences["user_id"] = "user_123"
        mockPreferences["username"] = "testuser"

        val result = repository.getCurrentUser()

        assertNotNull(result)
        assertEquals("testuser", result!!.username)
        assertEquals("user_123", result.id)
        assertEquals("test_token", result.token)
    }

    @Test
    fun `isLoggedIn should return true when token exists`() = runTest {
        repository = createRepository()
        mockPreferences["auth_token"] = "some_token"

        val result = repository.isLoggedIn.first()

        assertTrue(result)
    }

    @Test
    fun `isLoggedIn should return false when no token`() = runTest {
        repository = createRepository()
        mockPreferences.clear()

        val result = repository.isLoggedIn.first()

        assertFalse(result)
    }

    @Test
    fun `currentUsername should return username when available`() = runTest {
        repository = createRepository()
        mockPreferences["username"] = "testuser"

        val result = repository.currentUsername.first()

        assertEquals("testuser", result)
    }

    @Test
    fun `currentUsername should return null when not set`() = runTest {
        repository = createRepository()
        mockPreferences.clear()

        val result = repository.currentUsername.first()

        assertNull(result)
    }
}
