package mikhail.shell.video.hosting.presentation.video.screen

import android.media.session.PlaybackState
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.usecases.channels.Subscribe
import mikhail.shell.video.hosting.domain.usecases.videos.DeleteVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoDetails
import mikhail.shell.video.hosting.domain.usecases.videos.IncrementViews
import mikhail.shell.video.hosting.domain.usecases.videos.RateVideo

@HiltViewModel(assistedFactory = VideoScreenViewModel.Factory::class)
class VideoScreenViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    @Assisted("videoId") private val videoId: Long,
    private val _getVideoDetails: GetVideoDetails,
    private val _rateVideo: RateVideo,
    private val _subscribe: Subscribe,
    private val _incrementViews: IncrementViews,
    private val _deleteVideo: DeleteVideo
) : ViewModel() {
    private val _state = MutableStateFlow(VideoScreenState())
    val state = _state.asStateFlow()
    init {
        loadVideo()
    }
    @OptIn(UnstableApi::class)
    fun loadVideo() {
        _state.value = VideoScreenState()
        viewModelScope.launch {
            _getVideoDetails(
                videoId,
                userId
            ).onSuccess {
                _state.value = VideoScreenState(
                    videoDetails = it,
                    error = null,
                    isLoading = false
                )
            }.onFailure {
                _state.value = VideoScreenState(
                    videoDetails = _state.value.videoDetails,
                    isLoading = false,
                    error = it
                )
            }
        }
    }
    fun subscribe(subscriptionState: SubscriptionState) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            val channelId = _state.value.videoDetails?.channel?.channelId!!
            _subscribe(channelId, userId, subscriptionState).onSuccess { channelWithUser ->
                _state.update {
                    it.copy(
                        videoDetails = it.videoDetails?.copy(
                            channel = channelWithUser
                        ),
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        videoDetails = null,
                        isLoading = false,
                        error = VideoError.FAILED_LOADING
                    )
                }
            }
        }
    }

    fun rate(likingState: LikingState) {
        viewModelScope.launch {
            _rateVideo(
                videoId,
                userId,
                likingState
            ).onSuccess { updVideo ->
                _state.update { screenState ->
                    screenState.copy(
                        videoDetails = screenState.videoDetails?.copy(
                            video = screenState.videoDetails.video.copy(
                                likes = updVideo.likes,
                                dislikes = updVideo.dislikes,
                                liking = likingState
                            )
                        )
                    )
                }
            }.onFailure {
                _state
            }
        }
    }

    fun incrementViews() {
        viewModelScope.launch {
            _incrementViews(videoId).onSuccess { newViews ->
                _state.update {
                    it.copy(
                        videoDetails = it.videoDetails?.copy(
                            video = it.videoDetails.video.copy(
                                views = newViews
                            )
                        )
                    )
                }
            }.onFailure {

            }
        }
    }

    fun deleteVideo() {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _deleteVideo(videoId).onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        videoDetails = null
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: Long,
            @Assisted("videoId") videoId: Long
        ): VideoScreenViewModel
    }
}