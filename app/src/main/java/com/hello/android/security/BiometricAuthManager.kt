package com.hello.android.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import timber.log.Timber

/**
 * Manages biometric authentication for the app.
 * Supports fingerprint, face, and other biometric authentication methods.
 */
class BiometricAuthManager(private val context: Context) {

    private val biometricManager = BiometricManager.from(context)

    /**
     * Check if biometric authentication is available on this device.
     */
    fun canAuthenticate(): BiometricStatus {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.UNKNOWN_ERROR
        }
    }

    /**
     * Show biometric authentication prompt.
     * @param activity The activity to show the prompt on
     * @param title The title of the prompt
     * @param subtitle The subtitle of the prompt
     * @param negativeButtonText Text for the negative/cancel button
     * @param onSuccess Callback when authentication succeeds
     * @param onError Callback when authentication fails with error
     * @param onFallback Called when user wants to use fallback (e.g., password)
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        subtitle: String = "Verify your identity to continue",
        negativeButtonText: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit,
        onFallback: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Timber.d("Biometric authentication succeeded")
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Timber.d("Biometric authentication error: $errorCode - $errString")
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED
                ) {
                    onFallback()
                } else {
                    onError(errorCode, errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Timber.d("Biometric authentication failed")
                // Don't call onError here - the system will retry automatically
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Check if the device has secure lock screen (password/PIN/pattern) configured.
     */
    fun hasDeviceCredential(): Boolean {
        val status = biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        return status == BiometricManager.BIOMETRIC_SUCCESS
    }

    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        HARDWARE_UNAVAILABLE,
        NOT_ENROLLED,
        UNKNOWN_ERROR
    }
}
