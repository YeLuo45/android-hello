package com.hello.android.analytics

import android.content.Context
import android.content.SharedPreferences
import com.hello.android.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

/**
 * Local analytics service that logs events and stores them in SharedPreferences.
 * For production, replace with Firebase Analytics or Mixpanel by updating AnalyticsModule.
 */
class LocalAnalytics(context: Context) : Analytics {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        Timber.d("LocalAnalytics initialized - events will be logged and stored locally")
    }

    override fun track(event: String, properties: Map<String, Any>?) {
        try {
            val timestamp = System.currentTimeMillis()
            val eventJson = JSONObject().apply {
                put("event", event)
                put("timestamp", timestamp)
                put("app_version", BuildConfig.VERSION_NAME)
                put("environment", BuildConfig.ENV_NAME)
                properties?.let { props ->
                    val propsJson = JSONObject()
                    props.forEach { (key, value) ->
                        when (value) {
                            is String -> propsJson.put(key, value)
                            is Int -> propsJson.put(key, value)
                            is Long -> propsJson.put(key, value)
                            is Double -> propsJson.put(key, value)
                            is Boolean -> propsJson.put(key, value)
                            else -> propsJson.put(key, value.toString())
                        }
                    }
                    put("properties", propsJson)
                }
            }

            // Store event
            val events = getStoredEvents()
            events.put(eventJson)
            prefs.edit().putString(KEY_EVENTS, events.toString()).apply()

            Timber.d("Tracked event: $event with properties: $properties")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track event: $event")
        }
    }

    override fun setUserProperty(key: String, value: Any) {
        try {
            prefs.edit().putString("${KEY_USER_PREFIX}$key", value.toString()).apply()
            Timber.d("Set user property: $key = $value")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set user property: $key")
        }
    }

    override fun identify(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
        Timber.d("Identified user: $userId")
    }

    override fun reset() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_EVENTS)
            .apply()
        Timber.d("Analytics reset")
    }

    private fun getStoredEvents(): JSONArray {
        val stored = prefs.getString(KEY_EVENTS, "[]")
        return try {
            JSONArray(stored)
        } catch (e: Exception) {
            JSONArray()
        }
    }

    companion object {
        private const val PREFS_NAME = "analytics_prefs"
        private const val KEY_EVENTS = "stored_events"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_PREFIX = "user_property_"
    }
}
