package com.hello.android.data.di

import android.content.Context
import androidx.room.Room
import com.hello.android.data.local.AppDatabase
import com.hello.android.data.local.dao.CounterDao
import com.hello.android.data.local.dao.PostDao
import com.hello.android.data.local.dao.UserPreferencesDao
import com.hello.android.data.local.migration_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hello_database"
        )
            .addMigrations(migration_1_2)
            .build()
    }

    @Provides
    fun provideCounterDao(database: AppDatabase): CounterDao {
        return database.counterDao()
    }

    @Provides
    fun provideUserPreferencesDao(database: AppDatabase): UserPreferencesDao {
        return database.userPreferencesDao()
    }

    @Provides
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }
}
