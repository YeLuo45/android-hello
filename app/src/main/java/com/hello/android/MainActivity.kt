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
import com.hello.android.data.preferences.UserPreferencesRepository
import com.hello.android.ui.components.HelloNavHost
import com.hello.android.ui.theme.HelloAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity onCreate - Environment: ${BuildConfig.ENV_NAME}")

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = com.hello.android.data.preferences.ThemeMode.SYSTEM)

            HelloAndroidTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelloNavHost()
                }
            }
        }
    }
}
