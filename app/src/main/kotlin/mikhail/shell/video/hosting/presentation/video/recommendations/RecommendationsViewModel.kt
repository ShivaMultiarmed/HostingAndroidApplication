package mikhail.shell.video.hosting.presentation.video.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.videos.GetRecommendations
import mikhail.shell.video.hosting.presentation.video.models.toUi
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val getRecommendations: GetRecommendations
): ViewModel() {
    private val _state = MutableStateFlow(RecommendationsScreenState())
    val state = _state
        .onStart {
            load()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = _state.value
        )

    fun onEvent(event: RecommendationsScreenUiEvent) {
        when (event) {
            RecommendationsScreenUiEvent.BottomReached -> load()
            RecommendationsScreenUiEvent.Restart -> {
                _state.update { RecommendationsScreenState() }
                load()
            }
            RecommendationsScreenUiEvent.Reload -> load()
            else -> Unit
        }
    }

    private fun load() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            getRecommendations(
                partIndex = (_state.value.videos?.size ?: 0).toLong() / PART_SIZE,
                partSize = PART_SIZE
            ).onSuccess { videos ->
                _state.update {
                    it.copy(
                        videos = ((it.videos?: emptyList()) + videos.map { it.toUi() }).distinctBy { it.videoId },
                        isLoading = false,
                        hasMore = videos.size == PART_SIZE,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error
                    )
                }
            }
        }
    }
    private companion object {
        const val PART_SIZE = 10
    }
}

sealed class RecommendationsScreenUiEvent {
    data object BottomReached: RecommendationsScreenUiEvent()
    data object Reload: RecommendationsScreenUiEvent()
    data object Restart: RecommendationsScreenUiEvent()
    data class ClickedVideo(val videoId: Long): RecommendationsScreenUiEvent()
}