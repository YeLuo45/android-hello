package com.hello.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "counter")
data class CounterEntity(
    @PrimaryKey
    val id: Int = 1,
    val count: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
