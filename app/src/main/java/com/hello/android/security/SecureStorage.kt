package com.hello.android.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber

/**
 * Secure storage implementation using EncryptedSharedPreferences.
 * All data stored via this class is encrypted at rest using AES-256 GCM.
 */
class SecureStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Store a string value securely.
     */
    fun putString(key: String, value: String) {
        try {
            sharedPreferences.edit().putString(key, value).apply()
            Timber.d("Stored secure string for key: $key")
        } catch (e: Exception) {
            Timber.e(e, "Failed to store secure string for key: $key")
        }
    }

    /**
     * Retrieve a string value.
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return try {
            sharedPreferences.getString(key, defaultValue)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve secure string for key: $key")
            defaultValue
        }
    }

    /**
     * Store an integer value securely.
     */
    fun putInt(key: String, value: Int) {
        try {
            sharedPreferences.edit().putInt(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "Failed to store secure int for key: $key")
        }
    }

    /**
     * Retrieve an integer value.
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return try {
            sharedPreferences.getInt(key, defaultValue)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve secure int for key: $key")
            defaultValue
        }
    }

    /**
     * Store a long value securely.
     */
    fun putLong(key: String, value: Long) {
        try {
            sharedPreferences.edit().putLong(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "Failed to store secure long for key: $key")
        }
    }

    /**
     * Retrieve a long value.
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return try {
            sharedPreferences.getLong(key, defaultValue)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve secure long for key: $key")
            defaultValue
        }
    }

    /**
     * Store a boolean value securely.
     */
    fun putBoolean(key: String, value: Boolean) {
        try {
            sharedPreferences.edit().putBoolean(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "Failed to store secure boolean for key: $key")
        }
    }

    /**
     * Retrieve a boolean value.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            sharedPreferences.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve secure boolean for key: $key")
            defaultValue
        }
    }

    /**
     * Remove a value.
     */
    fun remove(key: String) {
        try {
            sharedPreferences.edit().remove(key).apply()
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove secure value for key: $key")
        }
    }

    /**
     * Clear all stored values.
     */
    fun clear() {
        try {
            sharedPreferences.edit().clear().apply()
            Timber.d("Cleared all secure storage")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear secure storage")
        }
    }

    /**
     * Check if a key exists.
     */
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    companion object {
        private const val PREFS_NAME = "secure_prefs"

        // Keys for biometric authentication state
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_FIRST_LAUNCH_COMPLETED = "first_launch_completed"
        const val KEY_LAST_AUTH_TIMESTAMP = "last_auth_timestamp"
        const val KEY_AUTH_SESSION_DURATION = "auth_session_duration"
    }
}
