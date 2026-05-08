package com.hello.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

enum class ThemeMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromValue(value: Int): ThemeMode = entries.find { it.value == value } ?: SYSTEM
    }
}

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("system", "跟随系统"),
    ENGLISH("en", "English"),
    CHINESE("zh", "中文");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: SYSTEM
    }
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_THEME_MODE = intPreferencesKey("theme_mode")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
    }

    val userPreferences: Flow<UserPreferences> = context.userPrefsDataStore.data.map { prefs ->
        UserPreferences(
            themeMode = ThemeMode.fromValue(prefs[KEY_THEME_MODE] ?: 0),
            language = AppLanguage.fromCode(prefs[KEY_LANGUAGE] ?: "system")
        )
    }

    val themeMode: Flow<ThemeMode> = context.userPrefsDataStore.data.map { prefs ->
        ThemeMode.fromValue(prefs[KEY_THEME_MODE] ?: 0)
    }

    val language: Flow<AppLanguage> = context.userPrefsDataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[KEY_LANGUAGE] ?: "system")
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = themeMode.value
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.code
        }
    }

    suspend fun getThemeMode(): ThemeMode {
        return context.userPrefsDataStore.data.first()[KEY_THEME_MODE]?.let { ThemeMode.fromValue(it) } ?: ThemeMode.SYSTEM
    }

    suspend fun clearCache() {
        // No-op for now, placeholder for future cache clearing
    }
}
