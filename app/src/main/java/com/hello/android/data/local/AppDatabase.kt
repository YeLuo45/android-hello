package com.hello.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hello.android.data.local.dao.CounterDao
import com.hello.android.data.local.dao.UserPreferencesDao
import com.hello.android.data.local.entity.CounterEntity
import com.hello.android.data.local.entity.UserPreferencesEntity

@Database(
    entities = [CounterEntity::class, UserPreferencesEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun counterDao(): CounterDao
    abstract fun userPreferencesDao(): UserPreferencesDao
}
