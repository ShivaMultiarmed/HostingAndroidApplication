package mikhail.shell.video.hosting.presentation.video.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.usecases.videos.GetRecommendations
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoCoverUrl
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.utils.stateIn
import mikhail.shell.video.hosting.presentation.video.models.toUi
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val getRecommendations: GetRecommendations,
    private val getChannelLogoUrl: GetChannelLogoUrl,
    private val getVideoCoverUrl: GetVideoCoverUrl,
) : ViewModel() {
    private val _state = MutableStateFlow(RecommendationsScreenState())
    val state = _state.onStart { load(start = true) }.stateIn(_state.value)

    private val _events = MutableSharedFlow<RecommendationsScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: RecommendationsScreenAction) {
        when (action) {
            RecommendationsScreenAction.LoadNextPart -> load()
            RecommendationsScreenAction.Restart -> load(start = true)
            is RecommendationsScreenAction.ChooseVideo -> viewModelScope.launch {
                _events.emit(RecommendationsScreenEvent.VideoChosen(action.videoId))
            }
        }
    }

    private fun load(start: Boolean = false) {
        if (_state.value.isStarting || _state.value.isLoading) {
            return
        }
        _state.update {
            it.copy(
                isStarting = start,
                isLoading = !start
            )
        }
        viewModelScope.launch {
            getRecommendations(
                partIndex = if (start) 0 else _state.value.nextPartIndex,
                partSize = PART_SIZE
            ).onSuccess { videos ->
                _state.update {
                    it.copy(
                        videos = (((if (start) null else it.videos) ?: emptyList()) + videos.map {
                            it.toUi(
                                channelLogo = getChannelLogoUrl(
                                    channelId = it.channel.channelId,
                                    size = ImageSize.MEDIUM
                                ),
                                videoCover = getVideoCoverUrl(
                                    videoId = it.video.videoId,
                                    size = ImageSize.MEDIUM
                                )
                            )
                        }).distinctBy { it.videoId },
                        isStarting = false,
                        isLoading = false,
                        hasMore = videos.size == PART_SIZE,
                        nextPartIndex = (if (start) 0 else it.nextPartIndex) + 1,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isStarting = false,
                        isLoading = false,
                        error = error
                    )
                }
                viewModelScope.launch {
                    _events.emit(RecommendationsScreenEvent.Failure(error))
                }
            }
        }
    }

    private companion object {
        const val PART_SIZE = 10
    }
}

sealed class RecommendationsScreenAction {
    data object Restart : RecommendationsScreenAction()
    data object LoadNextPart : RecommendationsScreenAction()
    data class ChooseVideo(val videoId: Long) : RecommendationsScreenAction()
}

sealed class RecommendationsScreenEvent {
    data class VideoChosen(val videoId: Long): RecommendationsScreenEvent()
    data class Failure(val error: Error): RecommendationsScreenEvent()
}