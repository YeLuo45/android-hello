package com.hello.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hello.android.analytics.Analytics
import com.hello.android.data.local.dao.UserPreferencesDao
import com.hello.android.data.local.entity.UserPreferencesEntity
import com.hello.android.ui.i18n.AppLanguage
import com.hello.android.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
    private val analytics: Analytics
) : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(true)
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.SYSTEM)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = userPreferencesDao.getPreferencesOnce()
            if (prefs != null) {
                _themeMode.value = ThemeMode.fromValue(prefs.themeMode.toIntOrNull() ?: 2)
                _dynamicColorEnabled.value = prefs.dynamicColorEnabled.toBoolean()
                _language.value = AppLanguage.fromCode(prefs.language)
            } else {
                userPreferencesDao.insert(UserPreferencesEntity(
                    themeMode = ThemeMode.SYSTEM.value.toString(),
                    dynamicColorEnabled = "true",
                    language = AppLanguage.SYSTEM.code
                ))
            }
        }

        viewModelScope.launch {
            userPreferencesDao.getPreferences().collect { prefs ->
                prefs?.let {
                    _themeMode.value = ThemeMode.fromValue(it.themeMode.toIntOrNull() ?: 2)
                    _dynamicColorEnabled.value = it.dynamicColorEnabled.toBoolean()
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
                userPreferencesDao.updateThemeMode(mode.value.toString())
            } else {
                userPreferencesDao.insert(UserPreferencesEntity(
                    themeMode = mode.value.toString(),
                    dynamicColorEnabled = _dynamicColorEnabled.value.toString(),
                    language = _language.value.code
                ))
            }
            analytics.track("theme_changed", mapOf("previous_theme" to previousTheme, "new_theme" to mode.name))
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _dynamicColorEnabled.value = enabled
            val current = userPreferencesDao.getPreferencesOnce()
            if (current != null) {
                userPreferencesDao.updateDynamicColorEnabled(enabled.toString())
            } else {
                userPreferencesDao.insert(UserPreferencesEntity(
                    themeMode = _themeMode.value.value.toString(),
                    dynamicColorEnabled = enabled.toString(),
                    language = _language.value.code
                ))
            }
            analytics.track("dynamic_color_changed", mapOf("enabled" to enabled.toString()))
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            val previousLanguage = _language.value.code
            val current = userPreferencesDao.getPreferencesOnce()
            if (current != null) {
                userPreferencesDao.updateLanguage(language.code)
            } else {
                userPreferencesDao.insert(UserPreferencesEntity(
                    themeMode = _themeMode.value.value.toString(),
                    dynamicColorEnabled = _dynamicColorEnabled.value.toString(),
                    language = language.code
                ))
            }
            analytics.track("language_changed", mapOf("previous_language" to previousLanguage, "new_language" to language.code))
        }
    }
}
