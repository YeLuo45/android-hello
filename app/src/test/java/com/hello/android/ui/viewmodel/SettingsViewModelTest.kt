package com.hello.android.ui.viewmodel

import com.hello.android.analytics.Analytics
import com.hello.android.data.local.dao.UserPreferencesDao
import com.hello.android.data.local.entity.UserPreferencesEntity
import com.hello.android.ui.i18n.AppLanguage
import com.hello.android.ui.theme.ThemeMode
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockUserPreferencesDao: UserPreferencesDao
    private lateinit var mockAnalytics: Analytics
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockUserPreferencesDao = mockk(relaxed = true)
        mockAnalytics = mockk(relaxed = true)

        coEvery { mockUserPreferencesDao.getPreferencesOnce() } returns null
        coEvery { mockUserPreferencesDao.insert(any()) } just Runs
        every { mockUserPreferencesDao.getPreferences() } returns MutableStateFlow(null)

        viewModel = SettingsViewModel(
            userPreferencesDao = mockUserPreferencesDao,
            analytics = mockAnalytics
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial themeMode should be SYSTEM when no preferences exist`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun `initial dynamicColorEnabled should be true when no preferences exist`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, viewModel.dynamicColorEnabled.value)
    }

    @Test
    fun `initial language should be SYSTEM when no preferences exist`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(AppLanguage.SYSTEM, viewModel.language.value)
    }

    @Test
    fun `setThemeMode should update database and track analytics`() = runTest {
        // Given
        coEvery { mockUserPreferencesDao.getPreferencesOnce() } returns null
        coEvery { mockUserPreferencesDao.updateThemeMode(any()) } just Runs
        coEvery { mockUserPreferencesDao.insert(any()) } just Runs

        // When
        viewModel.setThemeMode(ThemeMode.DARK)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockUserPreferencesDao.insert(any<UserPreferencesEntity>()) }
        verify { mockAnalytics.track("theme_changed", mapOf("previous_theme" to "SYSTEM", "new_theme" to "DARK")) }
    }

    @Test
    fun `setThemeMode when preferences exist should update existing record`() = runTest {
        // Given
        val existingPrefs = UserPreferencesEntity(
            id = 1,
            themeMode = ThemeMode.LIGHT.value.toString(),
            dynamicColorEnabled = "true",
            language = AppLanguage.SYSTEM.code
        )
        coEvery { mockUserPreferencesDao.getPreferencesOnce() } returns existingPrefs
        coEvery { mockUserPreferencesDao.updateThemeMode(any()) } just Runs

        // When
        viewModel.setThemeMode(ThemeMode.DARK)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockUserPreferencesDao.updateThemeMode(ThemeMode.DARK.value.toString()) }
    }

    @Test
    fun `setDynamicColorEnabled should update database and track analytics`() = runTest {
        // Given
        coEvery { mockUserPreferencesDao.getPreferencesOnce() } returns null
        coEvery { mockUserPreferencesDao.updateDynamicColorEnabled(any()) } just Runs
        coEvery { mockUserPreferencesDao.insert(any()) } just Runs

        // When
        viewModel.setDynamicColorEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockUserPreferencesDao.insert(any<UserPreferencesEntity>()) }
        verify { mockAnalytics.track("dynamic_color_changed", mapOf("enabled" to "false")) }
    }

    @Test
    fun `setDynamicColorEnabled when preferences exist should update existing record`() = runTest {
        // Given
        val existingPrefs = UserPreferencesEntity(
            id = 1,
            themeMode = ThemeMode.SYSTEM.value.toString(),
            dynamicColorEnabled = "true",
            language = AppLanguage.SYSTEM.code
        )
        coEvery { mockUserPreferencesDao.getPreferencesOnce() } returns existingPrefs
        coEvery { mockUserPreferencesDao.updateDynamicColorEnabled(any()) } just Runs

        // When
        viewModel.setDynamicColorEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockUserPreferencesDao.updateDynamicColorEnabled("false") }
    }

    @Test
    fun `setLanguage should update database and track analytics`() = runTest {
        // Given
        coEvery { mockUserPreferencesDao.getPreferencesOnce() } returns null
        coEvery { mockUserPreferencesDao.updateLanguage(any()) } just Runs
        coEvery { mockUserPreferencesDao.insert(any()) } just Runs

        // When
        viewModel.setLanguage(AppLanguage.CHINESE)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockUserPreferencesDao.insert(any<UserPreferencesEntity>()) }
        verify { mockAnalytics.track("language_changed", mapOf("previous_language" to "system", "new_language" to "zh")) }
    }

    @Test
    fun `setLanguage when preferences exist should update existing record`() = runTest {
        // Given
        val existingPrefs = UserPreferencesEntity(
            id = 1,
            themeMode = ThemeMode.SYSTEM.value.toString(),
            dynamicColorEnabled = "true",
            language = AppLanguage.SYSTEM.code
        )
        coEvery { mockUserPreferencesDao.getPreferencesOnce() } returns existingPrefs
        coEvery { mockUserPreferencesDao.updateLanguage(any()) } just Runs

        // When
        viewModel.setLanguage(AppLanguage.ENGLISH)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockUserPreferencesDao.updateLanguage(AppLanguage.ENGLISH.code) }
    }

    @Test
    fun `init with existing preferences should load them correctly`() = runTest {
        // Given
        val existingPrefs = UserPreferencesEntity(
            id = 1,
            themeMode = ThemeMode.DARK.value.toString(),
            dynamicColorEnabled = "false",
            language = AppLanguage.CHINESE.code
        )
        coEvery { mockUserPreferencesDao.getPreferencesOnce() } returns existingPrefs
        every { mockUserPreferencesDao.getPreferences() } returns MutableStateFlow(existingPrefs)

        // When
        val newViewModel = SettingsViewModel(mockUserPreferencesDao, mockAnalytics)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(ThemeMode.DARK, newViewModel.themeMode.value)
        assertEquals(false, newViewModel.dynamicColorEnabled.value)
        assertEquals(AppLanguage.CHINESE, newViewModel.language.value)
    }
}
