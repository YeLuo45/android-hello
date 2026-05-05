package com.hello.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1,
    val themeMode: String = "SYSTEM"
)
