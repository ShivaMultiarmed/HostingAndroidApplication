package mikhail.shell.video.hosting.presentation.video.page

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoDetails
import mikhail.shell.video.hosting.domain.usecases.videos.RateVideo
import mikhail.shell.video.hosting.presentation.exoplayer.PlaybackState.*

@HiltViewModel(assistedFactory = VideoScreenViewModel.Factory::class)
class VideoScreenViewModel @AssistedInject constructor(
    @Assisted("player") val player: Player,
    @Assisted("userId") private val userId: Long,
    @Assisted("videoId") private val videoId: Long,
    private val _getVideoDetails: GetVideoDetails,
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
            _getVideoDetails(
                videoId,
                userId
            ).onSuccess {
                _state.value = VideoScreenState(
                    videoDetails = it,
                    isLoading = false,
                    playbackState = PLAYING,
                    error = null
                )
                player.prepare()
                val url = it.video.sourceUrl
                val uri = Uri.parse(url)
                player.setMediaItem(MediaItem.fromUri(uri))
                changePlaybackState()
            }.onFailure {
                _state.value = VideoScreenState(
                    videoDetails = _state.value.videoDetails,
                    isLoading = false,
                    playbackState = PAUSED,
                    error = it
                )
            }
        }
    }

    fun rate(likingState: LikingState) {
        viewModelScope.launch {
            _rateVideo(
                videoId,
                userId,
                likingState
            ).onSuccess { updatedVideoInfo ->
                _state.update { screenState ->
                    screenState.copy(
                        videoDetails = screenState.videoDetails?.copy(
                            video = updatedVideoInfo
                        )
                    )
                }
            }.onFailure {
                _state
            }
        }
    }

    fun changePlaybackState() {
        _state.update {
            if (it.playbackState == PAUSED) {
                player.play()
                it.copy(
                    playbackState = PLAYING
                )
            } else {
                player.pause()
                it.copy(
                    playbackState = PAUSED
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("player") player: Player, @Assisted("userId") userId: Long, @Assisted("videoId") videoId: Long): VideoScreenViewModel
    }
}