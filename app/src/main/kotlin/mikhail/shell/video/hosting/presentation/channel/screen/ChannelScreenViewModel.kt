package mikhail.shell.video.hosting.presentation.channel.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannelDetails
import mikhail.shell.video.hosting.domain.usecases.channels.RemoveChannel
import mikhail.shell.video.hosting.domain.usecases.channels.Subscribe
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoList
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import mikhail.shell.video.hosting.presentation.video.models.toUi

@HiltViewModel(assistedFactory = ChannelScreenViewModel.Factory::class)
class ChannelScreenViewModel @AssistedInject constructor(
    @Assisted("channelId") private val channelId: Long,
    private val getChannelDetails: GetChannelDetails,
    private val getVideoList: GetVideoList,
    private val subscribe: Subscribe,
    private val removeChannel: RemoveChannel
) : ViewModel() {

    private val _state = MutableStateFlow<ChannelScreenState>(ChannelScreenState.Idle)
    val state = _state.onStart {
        initialize()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = _state.value
    )

    fun onEvent(event: ChannelScreenUiEvent) {
        viewModelScope.launch {
            when (event) {
                ChannelScreenUiEvent.ReachedBottom -> loadVideos(start = false)
                ChannelScreenUiEvent.RestartVideos -> loadVideos(start = true)
                ChannelScreenUiEvent.Restart -> initialize()
                ChannelScreenUiEvent.Remove -> remove()
                is ChannelScreenUiEvent.Subscribe -> subscribe(event.subscription)
                else -> Unit
            }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            loadChannel()
            loadVideos(start = true)
        }
    }

    private suspend fun loadChannel() {
        _state.update {
            ChannelScreenState.Starting
        }
        getChannelDetails(channelId).onSuccess { channel ->
            _state.update {
                ChannelScreenState.Success(
                    channel = channel.toUi(),
                    videoState = VideoListState()
                )
            }
        }.onFailure { error ->
            _state.update {
                ChannelScreenState.Failure(error)
            }
        }
    }

    private suspend fun loadVideos(start: Boolean = false) {
        val currentState = _state.value as? ChannelScreenState.Success
        if (currentState == null) {
            return
        }
        _state.update {
            currentState.copy(
                videoState = currentState.videoState.copy(
                    isStarting = start,
                    isLoading = !start
                )
            )
        }
        getVideoList(
            channelId = channelId,
            partIndex = if (start) 0 else currentState.videoState.nextPartIndex,
            partSize = PART_SIZE
        ).onSuccess { videos ->
            _state.update {
                currentState.copy(
                    videoState = currentState.videoState.copy(
                        videos = ((if (start) null else currentState.videoState.videos) ?: emptyList()) + videos.map { it.toUi() },
                        hasMore = videos.size == PART_SIZE,
                        nextPartIndex = (if (start) 0 else currentState.videoState.nextPartIndex) + 1,
                        isStarting = false,
                        isLoading = false,
                        error = null
                    )
                )
            }
        }.onFailure { error ->
            _state.update {
                currentState.copy(
                    videoState = currentState.videoState.copy(
                        videos = currentState.videoState.videos,
                        error = error
                    )
                )
            }
        }
    }

    private fun subscribe(subscription: Subscription) {
        val currentState = _state.value as? ChannelScreenState.Success
        if (currentState == null) {
            return
        }
        viewModelScope.launch {
            subscribe(
                channelId = currentState.channel.channelId,
                subscription = subscription
            ).onSuccess { updatedChannel ->
                _state.update {
                    currentState.copy(channel = updatedChannel.toUi())
                }
            }
        }
    }

    private fun remove() {
        viewModelScope.launch {
            removeChannel(channelId).onSuccess {
                _state.update {
                    ChannelScreenState.Removed
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("channelId") channelId: Long): ChannelScreenViewModel
    }

    private companion object {
        const val PART_SIZE = 10
    }
}

sealed class ChannelScreenUiEvent {
    data object Restart : ChannelScreenUiEvent()
    data object RestartVideos : ChannelScreenUiEvent()
    data class Subscribe(val subscription: Subscription) : ChannelScreenUiEvent()
    data class ClickVideo(val videoId: Long) : ChannelScreenUiEvent()
    data object ReachedBottom : ChannelScreenUiEvent()
    data object Edit : ChannelScreenUiEvent()
    data object Remove : ChannelScreenUiEvent()
}