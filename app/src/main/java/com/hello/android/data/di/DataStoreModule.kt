package com.hello.android.data.di

import android.content.Context
import com.hello.android.data.datastore.CounterDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideCounterDataStore(@ApplicationContext context: Context): CounterDataStore {
        return CounterDataStore(context)
    }
}
