package com.hello.android.data.di

import com.hello.android.data.LoggerImpl
import com.hello.android.domain.Logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {

    @Binds
    @Singleton
    abstract fun bindLogger(impl: LoggerImpl): Logger
}
