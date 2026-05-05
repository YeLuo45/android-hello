package com.hello.android.data

import com.hello.android.data.local.dao.CounterDao
import com.hello.android.data.local.entity.CounterEntity
import com.hello.android.domain.Logger
import com.hello.android.domain.model.CounterModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterRepository @Inject constructor(
    private val counterDao: CounterDao,
    private val logger: Logger
) {
    val counterState: Flow<CounterModel> = counterDao.getCounter().map { entity ->
        entity?.let { CounterModel(count = it.count) } ?: CounterModel()
    }

    suspend fun increment() {
        val current = counterDao.getCounterOnce() ?: CounterEntity()
        counterDao.insert(current.copy(count = current.count + 1, lastUpdated = System.currentTimeMillis()))
        logger.log("Counter incremented to: ${current.count + 1}")
    }

    suspend fun reset() {
        counterDao.insert(CounterEntity())
        logger.log("Counter reset to 0")
    }
}
