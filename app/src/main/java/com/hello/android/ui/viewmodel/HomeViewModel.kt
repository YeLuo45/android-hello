package com.hello.android.ui.viewmodel

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hello.android.analytics.Analytics
import com.hello.android.data.remote.ApiService
import com.hello.android.data.remote.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@Parcelize
data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : Parcelable

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val savedStateHandle: SavedStateHandle,
    private val analytics: Analytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<HomeUiState>(KEY_HOME_UI_STATE) ?: HomeUiState()
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        analytics.track("screen_view", mapOf("screen" to "HomeScreen"))
    }

    fun loadPosts() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            savedStateHandle[KEY_HOME_UI_STATE] = _uiState.value
            try {
                val posts = apiService.getPosts()
                _uiState.value = _uiState.value.copy(posts = posts, isLoading = false)
                savedStateHandle[KEY_HOME_UI_STATE] = _uiState.value
                analytics.track("posts_loaded", mapOf("count" to posts.size))
            } catch (e: Exception) {
                Timber.e(e, "Failed to load posts")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
                savedStateHandle[KEY_HOME_UI_STATE] = _uiState.value
            }
        }
    }

    companion object {
        private const val KEY_HOME_UI_STATE = "home_ui_state"
    }
}
