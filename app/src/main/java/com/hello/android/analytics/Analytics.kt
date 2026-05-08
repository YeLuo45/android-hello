package com.hello.android.analytics

interface Analytics {
    fun track(event: String, properties: Map<String, Any>? = null)
    fun setUserProperty(key: String, value: Any)
    fun identify(userId: String)
    fun reset()
}
