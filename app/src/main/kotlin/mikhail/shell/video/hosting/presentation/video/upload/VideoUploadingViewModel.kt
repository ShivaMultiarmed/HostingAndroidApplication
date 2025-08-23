package mikhail.shell.video.hosting.presentation.video.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
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
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.usecases.channels.GetOwnedChannels
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateImage
import mikhail.shell.video.hosting.domain.usecases.videos.UploadVideo
import mikhail.shell.video.hosting.domain.usecases.videos.ValidateVideoSource
import mikhail.shell.video.hosting.domain.usecases.videos.validation.ValidateChannelId
import mikhail.shell.video.hosting.domain.utils.ValidateTitle

@HiltViewModel(assistedFactory = VideoUploadingViewModel.Factory::class)
class VideoUploadingViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    @Assisted("player") val player: Player,
    private val getOwnedChannels: GetOwnedChannels,
    private val validateChannel: ValidateChannelId,
    private val validateImage: ValidateImage,
    private val validateVideoSource: ValidateVideoSource,
    private val validateTitle: ValidateTitle,
    private val uploadVideo: UploadVideo
) : ViewModel() {
    private val _state =
        MutableStateFlow<VideoUploadingScreenState>(VideoUploadingScreenState.Loading)
    val state = _state
        .onStart {
            load()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = _state.value
        )

    fun onEvent(event: VideoUploadingScreenUiEvent) {
        viewModelScope.launch {
            when(event) {
                is VideoUploadingScreenUiEvent.ChannelChanged -> onChannelChanged(event.channelId)
                is VideoUploadingScreenUiEvent.CoverChanged -> onCoverChanged(event.cover)
                VideoUploadingScreenUiEvent.Reload -> load()
                is VideoUploadingScreenUiEvent.SourceChanged -> onSourceChanged(event.source)
                VideoUploadingScreenUiEvent.Submit -> upload()
                is VideoUploadingScreenUiEvent.TitleChanged -> onTitleChanged(event.title)
                else -> null
            }
        }
    }

    private fun onChannelChanged(channelId: Long?) {
        _state.update {
            it as VideoUploadingScreenState.Editing
            it.copy(
                input = it.input.copy(
                    channelId = channelId,
                    channelError = channelId.let {
                        val validationResult = validateChannel(channelId)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onCoverChanged(cover: String?) {
        _state.update {
            it as VideoUploadingScreenState.Editing
            it.copy(
                input = it.input.copy(
                    cover = cover,
                    coverError = cover?.let {
                        val validationResult = validateImage(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onSourceChanged(source: String?) {
        _state.update {
            it as VideoUploadingScreenState.Editing
            it.copy(
                input = it.input.copy(
                    source = source,
                    sourceError = source.let {
                        val validationResult = validateVideoSource(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            it as VideoUploadingScreenState.Editing
            it.copy(
                input = it.input.copy(
                    title = title,
                    titleError = title.let {
                        val validationResult = validateTitle(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun upload() {
        val input = (_state.value as VideoUploadingScreenState.Editing).input
        viewModelScope.launch {
            uploadVideo(
                video = Video(
                    channelId = input.channelId!!,
                    description = input.description,
                    title = input.title,
                ),
                cover = input.cover
            ).onSuccess { video ->
                _state.update {
                    it as VideoUploadingScreenState.Editing
                    VideoUploadingScreenState.Success(
                        videoId = video.videoId!!,
                        source = it.input.source!!
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it as VideoUploadingScreenState.Editing
                    it.copy(error = error)
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            getOwnedChannels(userId)
                .onSuccess { channels ->
                    _state.update {
                        VideoUploadingScreenState.Editing(
                            channels = channels.map {
                                ChannelOptionUi(
                                    channelId = it.channelId!!,
                                    title = it.title
                                )
                            },
                            input = VideoUploadingInput()
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        VideoUploadingScreenState.Failure(error)
                    }
                }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: Long,
            @Assisted("player") player: Player
        ): VideoUploadingViewModel
    }
}

sealed class VideoUploadingScreenUiEvent {
    data object Cancel: VideoUploadingScreenUiEvent()
    data object Reload: VideoUploadingScreenUiEvent()
    data class TitleChanged(val title: String): VideoUploadingScreenUiEvent()
    data class SourceChanged(val source: String?): VideoUploadingScreenUiEvent()
    data class CoverChanged(val cover: String?): VideoUploadingScreenUiEvent()
    data class ChannelChanged(val channelId: Long?): VideoUploadingScreenUiEvent()
    data object Submit: VideoUploadingScreenUiEvent()
    data class Success(val videoId: Long, val source: String): VideoUploadingScreenUiEvent()
}