package mikhail.shell.video.hosting.presentation.channel.screen

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
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannelInfo
import mikhail.shell.video.hosting.domain.usecases.channels.Subscribe
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoList

@HiltViewModel(assistedFactory = ChannelScreenViewModel.Factory::class)
class ChannelScreenViewModel @AssistedInject constructor(
    @Assisted("channelId") private val _channelId: Long,
    @Assisted("userId") private val _userId: Long,
    private val _getChannelInfo: GetChannelInfo,
    private val _getVideoList: GetVideoList,
    private val _subscribe: Subscribe
) : ViewModel() {
    private val _state = MutableStateFlow(ChannelScreenState())
    val state = _state.asStateFlow()

    init {
        loadChannelInfo()
        loadVideosPart()
    }

    fun loadChannelInfo() {
        _state.update {
            it.copy(
                isChannelLoading = true
            )
        }
        viewModelScope.launch {
            _getChannelInfo(
                _channelId,
                _userId
            ).onSuccess { channelWithUser ->
                _state.update {
                    it.copy(
                        channel = channelWithUser,
                        isChannelLoading = false,
                        channelLoadingError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isChannelLoading = false,
                        channelLoadingError = error
                    )
                }
            }
        }
    }

    fun loadVideosPart() {
        _state.update {
            it.copy(
                areVideosLoading = true
            )
        }
        viewModelScope.launch {
            _getVideoList(
                _channelId,
                _userId,
                1,
                10
            ).onSuccess { videos ->
                _state.update {
                    it.copy(
                        areVideosLoading = false,
                        videos = (it.videos ?: listOf()) + videos,
                        videosLoadingError = null
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        areVideosLoading = false,
                        videosLoadingError = err
                    )
                }
            }
        }
    }
    fun subscribe(subscriptionState: SubscriptionState) {
        viewModelScope.launch {
            _subscribe(_channelId, _userId, subscriptionState).onSuccess { updatedChannelWithUser ->
                _state.update {
                    it.copy(
                        channel = updatedChannelWithUser
                    )
                }
            }.onFailure {

            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("channelId") channelId: Long, @Assisted("userId") userId: Long) : ChannelScreenViewModel
    }
}