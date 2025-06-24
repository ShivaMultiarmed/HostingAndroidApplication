package mikhail.shell.video.hosting.presentation.video.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoRecommendations

@HiltViewModel(assistedFactory = VideoRecommendationsViewModel.Factory::class)
class VideoRecommendationsViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val _getVideoRecommendations: GetVideoRecommendations
): ViewModel() {
    private val _mutableStateFlow = MutableStateFlow(VideoRecommendationsScreenState())
    val stateFlow = _mutableStateFlow.asStateFlow()
    fun loadVideosPart(partIndex: Long, partSize: Int) {
        _mutableStateFlow.update {
            it.copy(
                areVideosLoading = true
            )
        }
        viewModelScope.launch {
            _getVideoRecommendations(
                userId,
                partIndex,
                partSize
            ).onSuccess { videoSet ->
                _mutableStateFlow.update {
                    it.copy(
                        videos = ((it.videos?: emptySet()) + videoSet)
                            .sortedByDescending { it.video.dateTime }
                            .toSet(),
                        nextVideosPartIndex = it.nextVideosPartIndex + 1,
                        areVideosLoading = false,
                        areAllVideosLoaded = videoSet.size < 10,
                        videosLoadingError = null
                    )
                }
            }.onFailure { error ->
                _mutableStateFlow.update {
                    it.copy(
                        areVideosLoading = false,
                        videosLoadingError = error
                    )
                }
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): VideoRecommendationsViewModel
    }
}