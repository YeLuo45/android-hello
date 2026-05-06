package com.hello.android.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hello.android.data.CounterRepository
import com.hello.android.domain.Logger
import com.hello.android.domain.model.CounterModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val counterRepository: CounterRepository,
    private val logger: Logger,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val counterState: StateFlow<CounterModel> = counterRepository.counterState

    init {
        logger.log("MainViewModel initialized with savedStateHandle")
        savedStateHandle.get<String>(KEY_VIEW_MODEL_INIT)?.let {
            logger.log("MainViewModel restored from saved state")
        } ?: run {
            savedStateHandle[KEY_VIEW_MODEL_INIT] = "initialized"
        }
    }

    fun increment() {
        viewModelScope.launch {
            counterRepository.increment()
        }
    }

    fun reset() {
        viewModelScope.launch {
            counterRepository.reset()
        }
    }

    companion object {
        private const val KEY_VIEW_MODEL_INIT = "main_view_model_init"
    }
}
