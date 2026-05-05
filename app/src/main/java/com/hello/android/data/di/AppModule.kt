package com.hello.android.data.di

import android.content.Context
import androidx.room.Room
import com.hello.android.data.CounterRepository
import com.hello.android.data.LoggerImpl
import com.hello.android.data.local.AppDatabase
import com.hello.android.data.local.dao.CounterDao
import com.hello.android.data.local.dao.UserPreferencesDao
import com.hello.android.domain.Logger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindLogger(impl: LoggerImpl): Logger

    @Binds
    @Singleton
    abstract fun bindCounterRepository(impl: CounterRepository): CounterRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "hello_database"
            ).build()
        }

        @Provides
        fun provideCounterDao(database: AppDatabase): CounterDao {
            return database.counterDao()
        }

        @Provides
        fun provideUserPreferencesDao(database: AppDatabase): UserPreferencesDao {
            return database.userPreferencesDao()
        }
    }
}
