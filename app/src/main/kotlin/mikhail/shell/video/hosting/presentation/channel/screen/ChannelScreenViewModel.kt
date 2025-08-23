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

    private val _state = MutableStateFlow<ChannelScreenState>(ChannelScreenState.Loading)
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
                ChannelScreenUiEvent.ReachedBottom -> loadVideos()
                ChannelScreenUiEvent.Reload -> loadChannel()
                ChannelScreenUiEvent.Remove -> remove()
                is ChannelScreenUiEvent.Subscribe -> subscribe(event.subscription)
                else -> Unit
            }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            loadChannel()
            loadVideos()
        }
    }

    private suspend fun loadChannel() {
        getChannelDetails(channelId)
            .onSuccess { channel ->
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

    private suspend fun loadVideos() {
        _state.value as ChannelScreenState.Success
        getVideoList(
            channelId = channelId,
            partNumber = ((_state.value as ChannelScreenState.Success).videoState.videos?.size ?: 0).toLong() / PART_SIZE,
            partSize = PART_SIZE
        ).onSuccess { videos ->
            _state.update {
                it as ChannelScreenState.Success
                it.copy(
                    videoState = it.videoState.copy(
                        videos = (it.videoState.videos?: emptyList()) + videos.map { it.toUi() },
                        hasMore = videos.size % PART_SIZE == 0
                    )
                )
            }
        }.onFailure { error ->
            _state.update {
                it as ChannelScreenState.Success
                it.copy(
                    videoState = it.videoState.copy(
                        videos = it.videoState.videos,
                        error = error
                    )
                )
            }
        }
    }

    private fun subscribe(subscription: Subscription) {
        viewModelScope.launch {
            subscribe(
                channelId = (_state.value as ChannelScreenState.Success).channel.channelId,
                subscription = subscription
            ).onSuccess { updatedChannel ->
                _state.update {
                    (it as ChannelScreenState.Success).copy(channel = updatedChannel.toUi())
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
    data object Reload: ChannelScreenUiEvent()
    data class Subscribe(val subscription: Subscription): ChannelScreenUiEvent()
    data class ClickVideo(val videoId: Long): ChannelScreenUiEvent()
    data object ReachedBottom: ChannelScreenUiEvent()
    data object Edit: ChannelScreenUiEvent()
    data object Remove: ChannelScreenUiEvent()
}