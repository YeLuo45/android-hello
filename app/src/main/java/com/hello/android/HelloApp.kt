package com.hello.android

import android.app.Application
import com.hello.android.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class HelloApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
    }
}
