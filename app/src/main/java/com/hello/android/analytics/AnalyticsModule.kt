package com.hello.android.analytics

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalytics(@ApplicationContext context: Context): Analytics {
        // Using LocalAnalytics for now - replace with Firebase/Mixpanel for production
        // To use Firebase:
        // 1. Add google-services.json to app/ directory
        // 2. Add 'com.google.gms:google-services:4.4.0' to buildscript classpath
        // 3. Add 'apply plugin: com.google.gms.google-services' at end of app/build.gradle
        // 4. Replace LocalAnalytics with FirebaseAnalyticsService
        return LocalAnalytics(context)
    }
}
