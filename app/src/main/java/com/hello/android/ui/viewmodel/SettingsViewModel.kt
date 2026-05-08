package com.hello.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hello.android.data.preferences.AppLanguage
import com.hello.android.data.preferences.ThemeMode
import com.hello.android.data.preferences.UserPreferences
import com.hello.android.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val cacheCleared: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemeMode.SYSTEM
    )

    val language: StateFlow<AppLanguage> = userPreferencesRepository.language.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppLanguage.SYSTEM
    )

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { prefs ->
                _uiState.value = _uiState.value.copy(preferences = prefs)
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(themeMode)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(language)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userPreferencesRepository.clearCache()
            _uiState.value = _uiState.value.copy(isLoading = false, cacheCleared = true)
        }
    }

    fun resetCacheCleared() {
        _uiState.value = _uiState.value.copy(cacheCleared = false)
    }
}
