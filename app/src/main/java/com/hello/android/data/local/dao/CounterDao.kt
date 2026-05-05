package com.hello.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hello.android.data.local.entity.CounterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {

    @Query("SELECT * FROM counter WHERE id = 1")
    fun getCounter(): Flow<CounterEntity?>

    @Query("SELECT * FROM counter WHERE id = 1")
    suspend fun getCounterOnce(): CounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(counter: CounterEntity)

    @Update
    suspend fun update(counter: CounterEntity)

    @Query("UPDATE counter SET count = :count, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateCount(count: Int, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM counter WHERE id = 1")
    suspend fun reset()
}
