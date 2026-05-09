package com.hello.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.android.security.BiometricAuthManager
import com.hello.android.security.SecureStorage
import com.hello.android.ui.components.HelloNavHost
import com.hello.android.ui.i18n.AppLanguage
import com.hello.android.ui.i18n.LocaleHelper
import com.hello.android.ui.theme.HelloAndroidTheme
import com.hello.android.ui.viewmodel.SettingsViewModel
import com.hello.android.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    @Inject
    lateinit var secureStorage: SecureStorage

    private var isAuthenticated = false
    private var pendingAuth = false
    private var contentShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install SplashScreen before super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity onCreate - Environment: ${BuildConfig.ENV_NAME}")

        // Check if biometric authentication is needed
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        val firstLaunchCompleted = secureStorage.getBoolean(SecureStorage.KEY_FIRST_LAUNCH_COMPLETED, false)
        val biometricEnabled = secureStorage.getBoolean(SecureStorage.KEY_BIOMETRIC_ENABLED, true)

        if (!firstLaunchCompleted) {
            // First launch - enable biometric and mark as completed
            secureStorage.putBoolean(SecureStorage.KEY_BIOMETRIC_ENABLED, true)
            secureStorage.putBoolean(SecureStorage.KEY_FIRST_LAUNCH_COMPLETED, true)
            isAuthenticated = true
            pendingAuth = false
            Timber.d("First launch - biometric authentication enabled")
        } else if (biometricEnabled) {
            // Check session timeout
            val lastAuthTime = secureStorage.getLong(SecureStorage.KEY_LAST_AUTH_TIMESTAMP, 0L)
            val sessionDuration = secureStorage.getLong(SecureStorage.KEY_AUTH_SESSION_DURATION, SESSION_TIMEOUT_MS)
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastAuthTime > sessionDuration) {
                // Session expired - require re-authentication
                pendingAuth = true
                isAuthenticated = false
                Timber.d("Session expired - re-authentication required")
            } else {
                isAuthenticated = true
                pendingAuth = false
            }
        } else {
            isAuthenticated = true
            pendingAuth = false
        }
    }

    private fun startBiometricAuthentication() {
        val biometricStatus = biometricAuthManager.canAuthenticate()

        when (biometricStatus) {
            BiometricAuthManager.BiometricStatus.AVAILABLE -> {
                biometricAuthManager.authenticate(
                    activity = this,
                    title = "Authentication Required",
                    subtitle = "Verify your identity to access the app",
                    negativeButtonText = "Cancel",
                    onSuccess = {
                        onAuthenticationSuccess()
                    },
                    onError = { errorCode, errorMessage ->
                        Timber.e("Biometric auth error: $errorCode - $errorMessage")
                        runOnUiThread {
                            if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                                errorCode == BiometricPrompt.ERROR_USER_CANCELED
                            ) {
                                Timber.d("User cancelled biometric authentication")
                            } else {
                                onAuthenticationSuccess()
                            }
                        }
                    },
                    onFallback = {
                        Timber.d("User selected fallback authentication")
                        onAuthenticationSuccess()
                    }
                )
            }
            BiometricAuthManager.BiometricStatus.NO_HARDWARE,
            BiometricAuthManager.BiometricStatus.HARDWARE_UNAVAILABLE,
            BiometricAuthManager.BiometricStatus.NOT_ENROLLED,
            BiometricAuthManager.BiometricStatus.UNKNOWN_ERROR -> {
                Timber.w("Biometric not available: $biometricStatus - allowing access without biometric")
                onAuthenticationSuccess()
            }
        }
    }

    private fun onAuthenticationSuccess() {
        isAuthenticated = true
        pendingAuth = false
        secureStorage.putLong(SecureStorage.KEY_LAST_AUTH_TIMESTAMP, System.currentTimeMillis())
        Timber.d("Authentication successful - session started")
        showMainContent()
    }

    private fun showLoadingScreen() {
        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    private fun showMainContent() {
        if (contentShown) return
        contentShown = true

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val dynamicColorEnabled by settingsViewModel.dynamicColorEnabled.collectAsState()
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

            HelloAndroidTheme(darkTheme = isDarkTheme, dynamicColor = dynamicColorEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelloNavHost(
                        currentTheme = themeMode,
                        onThemeChange = { settingsViewModel.setThemeMode(it) },
                        currentDynamicColor = dynamicColorEnabled,
                        onDynamicColorChange = { settingsViewModel.setDynamicColorEnabled(it) },
                        currentLanguage = language,
                        onLanguageChange = { settingsViewModel.setLanguage(it) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (isAuthenticated && !contentShown) {
            showMainContent()
        } else if (pendingAuth && !isAuthenticated) {
            pendingAuth = false
            startBiometricAuthentication()
        }
    }

    override fun onPause() {
        super.onPause()
        val timeSinceAuth = System.currentTimeMillis() - secureStorage.getLong(SecureStorage.KEY_LAST_AUTH_TIMESTAMP, 0L)
        if (timeSinceAuth > BACKGROUND_TIMEOUT_MS) {
            isAuthenticated = false
            contentShown = false
            Timber.d("App was in background - will require re-authentication")
        }
    }

    companion object {
        // Session timeout: 5 minutes
        private const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L

        // Background timeout: 30 seconds
        private const val BACKGROUND_TIMEOUT_MS = 30 * 1000L
    }
}
