package com.hello.android.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.counterDataStore: DataStore<Preferences> by preferencesDataStore(name = "counter_prefs")

class CounterDataStore(private val context: Context) {

    private val COUNTER_KEY = intPreferencesKey("counter_value")

    val counterFlow: Flow<Int> = context.counterDataStore.data.map { preferences ->
        preferences[COUNTER_KEY] ?: 0
    }

    suspend fun getCounter(): Int {
        var counter = 0
        context.counterDataStore.edit { preferences ->
            counter = preferences[COUNTER_KEY] ?: 0
        }
        return counter
    }

    suspend fun incrementCounter(): Int {
        var newValue = 0
        context.counterDataStore.edit { preferences ->
            val current = preferences[COUNTER_KEY] ?: 0
            newValue = current + 1
            preferences[COUNTER_KEY] = newValue
        }
        return newValue
    }

    suspend fun setCounter(value: Int) {
        context.counterDataStore.edit { preferences ->
            preferences[COUNTER_KEY] = value
        }
    }

    suspend fun resetCounter() {
        context.counterDataStore.edit { preferences ->
            preferences[COUNTER_KEY] = 0
        }
    }
}
