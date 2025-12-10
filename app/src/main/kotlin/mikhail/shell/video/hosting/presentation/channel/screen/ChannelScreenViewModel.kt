package mikhail.shell.video.hosting.presentation.channel.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannelDetails
import mikhail.shell.video.hosting.domain.usecases.channels.RemoveChannel
import mikhail.shell.video.hosting.domain.usecases.channels.Subscribe
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoCoverUrl
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoList
import mikhail.shell.video.hosting.domain.utils.GetChannelHeaderUrl
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import mikhail.shell.video.hosting.presentation.utils.stateIn
import mikhail.shell.video.hosting.presentation.video.models.toUi

@HiltViewModel(assistedFactory = ChannelScreenViewModel.Factory::class)
class ChannelScreenViewModel @AssistedInject constructor(
    @Assisted("channelId") private val channelId: Long,
    private val getChannelDetails: GetChannelDetails,
    private val getVideoList: GetVideoList,
    private val getChannelLogoUrl: GetChannelLogoUrl,
    private val getChannelHeaderUrl: GetChannelHeaderUrl,
    private val getVideoCoverUrl: GetVideoCoverUrl,
    private val subscribe: Subscribe,
    private val removeChannel: RemoveChannel
) : ViewModel() {
    private val _state = MutableStateFlow(ChannelScreenState())
    val state = _state.onStart { startAll() }.stateIn(_state.value)

    private val _events = MutableSharedFlow<ChannelScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ChannelScreenAction) {
        when (action) {
            ChannelScreenAction.LoadNextPart -> viewModelScope.launch {
                loadVideos(start = false)
            }
            ChannelScreenAction.RestartVideos -> viewModelScope.launch {
                loadVideos(start = true)
            }
            ChannelScreenAction.RestartChannel -> startAll()
            ChannelScreenAction.Remove -> remove()
            is ChannelScreenAction.Subscribe -> subscribe(action.subscription)
            is ChannelScreenAction.ChooseVideo -> viewModelScope.launch {
                _events.emit(ChannelScreenEvent.VideoChosen(action.videoId))
            }
            ChannelScreenAction.Edit -> viewModelScope.launch {
                _events.emit(ChannelScreenEvent.EditingRequest(channelId))
            }
        }
    }

    private fun startAll() {
        viewModelScope.launch {
            startChannel()
        }
        viewModelScope.launch {
            loadVideos(start = true)
        }
    }

    private suspend fun startChannel() {
        if (_state.value.isStarting) {
            return
        }
        _state.update {
            it.copy(isStarting = true)
        }
        getChannelDetails(channelId).onSuccess { channel ->
            _state.update {
                it.copy(
                    channel = channel.toUi(
                        logo = getChannelLogoUrl(channelId = channelId, size = ImageSize.MEDIUM),
                        header = getChannelHeaderUrl(channelId = channelId, size = ImageSize.LARGE)
                    ),
                    isStarting = false
                )
            }
        }.onFailure { error ->
            _state.update {
                it.copy(
                    error = error,
                    isStarting = false
                )
            }
            viewModelScope.launch {
                _events.emit(ChannelScreenEvent.Failure(error))
            }
        }
    }

    private suspend fun loadVideos(start: Boolean = false) {
        if (_state.value.videos.isStarting || _state.value.videos.isLoading) {
            return
        }
        _state.update {
            it.copy(
                videos = it.videos.copy(
                    isStarting = start,
                    isLoading = !start
                )
            )
        }
        getVideoList(
            channelId = channelId,
            partIndex = if (start) 0 else _state.value.videos.nextPartIndex,
            partSize = PART_SIZE
        ).onSuccess { videos ->
            _state.update {
                it.copy(
                    videos = it.videos.copy(
                        videos = ((if (start) null else it.videos.videos)
                            ?: emptyList()) + videos.map {
                            it.toUi(
                                cover = getVideoCoverUrl(
                                    videoId = it.videoId,
                                    size = ImageSize.LARGE
                                )
                            )
                        },
                        hasMore = videos.size == PART_SIZE,
                        nextPartIndex = (if (start) 0 else it.videos.nextPartIndex) + 1,
                        isStarting = false,
                        isLoading = false,
                        error = null
                    )
                )
            }
        }.onFailure { error ->
            _state.update {
                it.copy(
                    videos = it.videos.copy(
                        videos = it.videos.videos,
                        error = error,
                        isLoading = false,
                        isStarting = false
                    )
                )
            }
            viewModelScope.launch {
                _events.emit(ChannelScreenEvent.Failure(error))
            }
        }
    }

    private fun subscribe(subscription: Subscription) {
        if (_state.value.isSubscribing) {
            return
        }
        _state.update {
            it.copy(isSubscribing = true)
        }
        viewModelScope.launch {
            subscribe(
                channelId = _state.value.channel!!.channelId,
                subscription = subscription
            ).onSuccess { updatedChannel ->
                _state.update {
                    it.copy(
                        channel = it.channel!!.copy(
                            subscription = updatedChannel.subscription,
                            subscribers = updatedChannel.subscribers,
                        ),
                        error = null,
                        isSubscribing = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error,
                        isSubscribing = false
                    )
                }
                viewModelScope.launch {
                    _events.emit(ChannelScreenEvent.Failure(error))
                }
            }
        }
    }

    private fun remove() {
        if (_state.value.isRemoving) {
            return
        }
        viewModelScope.launch {
            removeChannel(channelId).onSuccess {
                _state.update {
                    it.copy(isRemoving = false)
                }
                viewModelScope.launch {
                    _events.emit(ChannelScreenEvent.Removed(channelId))
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error,
                        isRemoving = false
                    )
                }
                viewModelScope.launch {
                    _events.emit(ChannelScreenEvent.Failure(error))
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