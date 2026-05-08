package com.hello.android.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

data class User(
    val id: String,
    val username: String,
    val token: String
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
    }

    val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { prefs ->
        prefs[KEY_TOKEN] != null
    }

    val currentUsername: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_USERNAME]
    }

    suspend fun getToken(): String? {
        return context.authDataStore.data.first()[KEY_TOKEN]
    }

    suspend fun login(username: String, password: String): AuthResult {
        // Mock login - accept any non-empty credentials
        if (username.isBlank() || password.isBlank()) {
            return AuthResult.Error("Username and password cannot be empty")
        }
        
        // Mock: accept password "password" or any length >= 6
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }

        val token = "mock_token_${System.currentTimeMillis()}"
        val user = User(
            id = "user_${username.hashCode()}",
            username = username,
            token = token
        )

        saveAuthData(user)
        return AuthResult.Success(user)
    }

    suspend fun register(username: String, password: String, confirmPassword: String): AuthResult {
        if (username.isBlank()) {
            return AuthResult.Error("Username cannot be empty")
        }
        if (username.length < 3) {
            return AuthResult.Error("Username must be at least 3 characters")
        }
        if (password.isBlank()) {
            return AuthResult.Error("Password cannot be empty")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }
        if (password != confirmPassword) {
            return AuthResult.Error("Passwords do not match")
        }

        val token = "mock_token_${System.currentTimeMillis()}"
        val user = User(
            id = "user_${username.hashCode()}",
            username = username,
            token = token
        )

        saveAuthData(user)
        return AuthResult.Success(user)
    }

    private suspend fun saveAuthData(user: User) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_TOKEN] = user.token
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USERNAME] = user.username
        }
    }

    suspend fun logout() {
        context.authDataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USERNAME)
        }
    }

    suspend fun getCurrentUser(): User? {
        val prefs = context.authDataStore.data.first()
        val token = prefs[KEY_TOKEN] ?: return null
        val userId = prefs[KEY_USER_ID] ?: return null
        val username = prefs[KEY_USERNAME] ?: return null
        return User(userId, username, token)
    }
}
