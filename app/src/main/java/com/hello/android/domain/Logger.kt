package com.hello.android.domain

/**
 * Simple Logger interface for domain layer
 */
interface Logger {
    fun log(message: String, tag: String = "HelloApp")
}
