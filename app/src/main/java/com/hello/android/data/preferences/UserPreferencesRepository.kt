package com.hello.android.data.preferences

import com.hello.android.data.local.dao.UserPreferencesDao
import com.hello.android.data.local.entity.UserPreferencesEntity
import com.hello.android.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao
) {
    val userPreferences: Flow<UserPreferences> = userPreferencesDao.getPreferences().map { prefs ->
        prefs?.let {
            UserPreferences(
                themeMode = ThemeMode.fromValue(it.themeMode.toIntOrNull() ?: 2),
                dynamicColorEnabled = it.dynamicColorEnabled.toBoolean(),
                language = AppLanguage.fromCode(it.language)
            )
        } ?: UserPreferences()
    }

    val themeMode: Flow<ThemeMode> = userPreferencesDao.getPreferences().map { prefs ->
        prefs?.themeMode?.toIntOrNull()?.let { ThemeMode.fromValue(it) } ?: ThemeMode.SYSTEM
    }

    val dynamicColorEnabled: Flow<Boolean> = userPreferencesDao.getPreferences().map { prefs ->
        prefs?.dynamicColorEnabled?.toBoolean() ?: true
    }

    val language: Flow<AppLanguage> = userPreferencesDao.getPreferences().map { prefs ->
        prefs?.language?.let { AppLanguage.fromCode(it) } ?: AppLanguage.SYSTEM
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        val current = userPreferencesDao.getPreferencesOnce()
        if (current != null) {
            userPreferencesDao.updateThemeMode(themeMode.value.toString())
        } else {
            userPreferencesDao.insert(UserPreferencesEntity(
                themeMode = themeMode.value.toString(),
                dynamicColorEnabled = "true",
                language = AppLanguage.SYSTEM.code
            ))
        }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        val current = userPreferencesDao.getPreferencesOnce()
        if (current != null) {
            userPreferencesDao.updateDynamicColorEnabled(enabled.toString())
        } else {
            userPreferencesDao.insert(UserPreferencesEntity(
                themeMode = ThemeMode.SYSTEM.value.toString(),
                dynamicColorEnabled = enabled.toString(),
                language = AppLanguage.SYSTEM.code
            ))
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        val current = userPreferencesDao.getPreferencesOnce()
        if (current != null) {
            userPreferencesDao.updateLanguage(language.code)
        } else {
            userPreferencesDao.insert(UserPreferencesEntity(
                themeMode = ThemeMode.SYSTEM.value.toString(),
                dynamicColorEnabled = "true",
                language = language.code
            ))
        }
    }

    suspend fun getThemeMode(): ThemeMode {
        return userPreferencesDao.getPreferencesOnce()?.themeMode?.toIntOrNull()?.let { ThemeMode.fromValue(it) } ?: ThemeMode.SYSTEM
    }

    suspend fun clearCache() {
        userPreferencesDao.insert(UserPreferencesEntity())
    }
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.SYSTEM
)
