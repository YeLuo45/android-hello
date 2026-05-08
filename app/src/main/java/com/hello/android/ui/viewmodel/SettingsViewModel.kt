package com.hello.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hello.android.analytics.Analytics
import com.hello.android.data.local.dao.UserPreferencesDao
import com.hello.android.data.local.entity.UserPreferencesEntity
import com.hello.android.ui.i18n.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
    private val analytics: Analytics
) : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.SYSTEM)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = userPreferencesDao.getPreferencesOnce()
            if (prefs != null) {
                _themeMode.value = try {
                    ThemeMode.valueOf(prefs.themeMode)
                } catch (e: IllegalArgumentException) {
                    ThemeMode.SYSTEM
                }
                _language.value = AppLanguage.fromCode(prefs.language)
            } else {
                userPreferencesDao.insert(UserPreferencesEntity(themeMode = ThemeMode.SYSTEM.name, language = AppLanguage.SYSTEM.code))
            }
        }

        viewModelScope.launch {
            userPreferencesDao.getPreferences().collect { prefs ->
                prefs?.let {
                    _themeMode.value = try {
                        ThemeMode.valueOf(it.themeMode)
                    } catch (e: IllegalArgumentException) {
                        ThemeMode.SYSTEM
                    }
                    _language.value = AppLanguage.fromCode(it.language)
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            val previousTheme = _themeMode.value.name
            val current = userPreferencesDao.getPreferencesOnce()
            if (current != null) {
                userPreferencesDao.updateThemeMode(mode.name)
            } else {
                userPreferencesDao.insert(UserPreferencesEntity(themeMode = mode.name, language = _language.value.code))
            }
            analytics.track("theme_changed", mapOf("previous_theme" to previousTheme, "new_theme" to mode.name))
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            val previousLanguage = _language.value.code
            val current = userPreferencesDao.getPreferencesOnce()
            if (current != null) {
                userPreferencesDao.updateLanguage(language.code)
            } else {
                userPreferencesDao.insert(UserPreferencesEntity(themeMode = _themeMode.value.name, language = language.code))
            }
            analytics.track("language_changed", mapOf("previous_language" to previousLanguage, "new_language" to language.code))
        }
    }
}
