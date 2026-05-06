package com.hello.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.android.ui.components.HelloNavHost
import com.hello.android.ui.i18n.AppLanguage
import com.hello.android.ui.i18n.LocaleHelper
import com.hello.android.ui.theme.HelloAndroidTheme
import com.hello.android.ui.viewmodel.SettingsViewModel
import com.hello.android.ui.viewmodel.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Language is handled via recomposition, not attachBaseContext
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity onCreate - Environment: ${BuildConfig.ENV_NAME}")

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val language by settingsViewModel.language.collectAsState()

            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            // Apply locale context for string resources
            val localeContext = if (language != AppLanguage.SYSTEM) {
                LocaleHelper.setLocale(this, language)
            } else {
                this
            }

            HelloAndroidTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelloNavHost(
                        currentTheme = themeMode,
                        onThemeChange = { settingsViewModel.setThemeMode(it) },
                        currentLanguage = language,
                        onLanguageChange = { settingsViewModel.setLanguage(it) }
                    )
                }
            }
        }
    }
}
