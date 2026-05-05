package com.hello.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.hello.android.ui.components.HelloNavHost
import com.hello.android.ui.theme.HelloAndroidTheme
import com.hello.android.ui.viewmodel.SettingsViewModel
import com.hello.android.ui.viewmodel.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.lifecycle.viewmodel.compose.viewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity onCreate - Environment: ${BuildConfig.ENV_NAME}")

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            HelloAndroidTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelloNavHost(
                        currentTheme = themeMode,
                        onThemeChange = { settingsViewModel.setThemeMode(it) }
                    )
                }
            }
        }
    }
}
