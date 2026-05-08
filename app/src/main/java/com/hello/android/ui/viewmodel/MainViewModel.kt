package com.hello.android.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hello.android.analytics.Analytics
import com.hello.android.data.CounterRepository
import com.hello.android.data.datastore.CounterDataStore
import com.hello.android.domain.Logger
import com.hello.android.domain.model.CounterModel
import com.hello.android.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val counterRepository: CounterRepository,
    private val logger: Logger,
    private val savedStateHandle: SavedStateHandle,
    private val analytics: Analytics,
    private val counterDataStore: CounterDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val counterState: StateFlow<CounterModel> = counterRepository.counterState

    init {
        logger.log("MainViewModel initialized with savedStateHandle")
        savedStateHandle.get<String>(KEY_VIEW_MODEL_INIT)?.let {
            logger.log("MainViewModel restored from saved state")
        } ?: run {
            savedStateHandle[KEY_VIEW_MODEL_INIT] = "initialized"
        }

        // Sync with widget counter value on startup
        syncWithWidgetCounter()
    }

    private fun syncWithWidgetCounter() {
        viewModelScope.launch {
            // Get widget counter from DataStore
            val widgetCounter = counterDataStore.counterFlow.first()
            val currentCounter = counterState.value.count
            // If widget counter is ahead, sync it
            if (widgetCounter > currentCounter) {
                repeat(widgetCounter - currentCounter) {
                    counterRepository.increment()
                }
            }
        }
    }

    fun increment() {
        viewModelScope.launch {
            counterRepository.increment()
            checkCounterMilestone()
            analytics.track("counter_increment", mapOf("count" to counterState.value.count))
        }
    }

    private fun checkCounterMilestone() {
        val count = counterState.value.count
        // Notify at 10, 100, 1000, 10000, etc.
        if (count > 0 && count % 10 == 0) {
            NotificationHelper.showCounterNotification(context, count)
        }
    }

    fun reset() {
        viewModelScope.launch {
            val previousCount = counterState.value.count
            counterRepository.reset()
            analytics.track("counter_reset", mapOf("previous_count" to previousCount))
        }
    }

    companion object {
        private const val KEY_VIEW_MODEL_INIT = "main_view_model_init"
    }
}
