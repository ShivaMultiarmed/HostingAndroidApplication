package mikhail.shell.video.hosting.presentation.video.page

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
import mikhail.shell.video.hosting.domain.usecases.videos.GetExtendedVideoInfo
import mikhail.shell.video.hosting.domain.usecases.videos.RateVideo

@HiltViewModel(assistedFactory = VideoScreenViewModel.Factory::class)
class VideoScreenViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    @Assisted("videoId") private val videoId: Long,
    private val _getExtendedVideoInfo: GetExtendedVideoInfo,
    private val _rateVideo: RateVideo
): ViewModel() {
    private val _state = MutableStateFlow(VideoScreenState())
    val state = _state.asStateFlow()

    init {
        loadVideo()
    }

    fun loadVideo() {
        _state.value = VideoScreenState()
        viewModelScope.launch {
            _getExtendedVideoInfo(
                videoId,
                userId
            ).onSuccess {
                _state.value = VideoScreenState(
                    extendedVideoInfo = it,
                    isLoading = false,
                    error = null
                )
            }.onFailure {
                _state.value = VideoScreenState(
                    extendedVideoInfo = _state.value.extendedVideoInfo,
                    isLoading = false,
                    error = it
                )
            }
        }
    }

    fun rate(liking: Boolean) {
        viewModelScope.launch {
            _rateVideo(
                videoId,
                userId,
                liking
            ).onSuccess { newLikeState ->
                _state.update {
                    it.copy(
                        extendedVideoInfo = it.extendedVideoInfo?.copy(
                            liking = newLikeState
                        )
                    )
                }
            }.onFailure {

            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long, @Assisted("videoId") videoId: Long): VideoScreenViewModel
    }
}