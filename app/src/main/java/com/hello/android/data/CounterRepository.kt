package com.hello.android.data

import com.hello.android.domain.Logger
import com.hello.android.domain.model.CounterModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterRepository @Inject constructor(
    private val logger: Logger
) {
    private val _counterState = MutableStateFlow(CounterModel())
    val counterState: StateFlow<CounterModel> = _counterState.asStateFlow()

    fun increment() {
        _counterState.value = _counterState.value.copy(count = _counterState.value.count + 1)
        logger.log("Counter incremented to: ${_counterState.value.count}")
    }

    fun reset() {
        _counterState.value = CounterModel()
        logger.log("Counter reset to 0")
    }
}
