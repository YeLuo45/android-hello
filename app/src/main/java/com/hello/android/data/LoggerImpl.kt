package com.hello.android.data

import com.hello.android.domain.Logger
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggerImpl @Inject constructor() : Logger {
    override fun log(message: String, tag: String) {
        Timber.tag(tag).d(message)
    }
}
