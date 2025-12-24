package mikhail.shell.video.hosting.presentation.video.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
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
import mikhail.shell.video.hosting.domain.errors.video.VideoUploadingError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoCreationModel
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.channels.GetOwnedChannels
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoMetaData
import mikhail.shell.video.hosting.domain.usecases.videos.UploadVideo
import mikhail.shell.video.hosting.domain.usecases.videos.validation.ValidateChannelId
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import mikhail.shell.video.hosting.domain.utils.ValidateTitle
import mikhail.shell.video.hosting.domain.utils.ValidateVideoSource
import mikhail.shell.video.hosting.presentation.utils.stateIn
import kotlin.uuid.ExperimentalUuidApi
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState as ScreenState

@HiltViewModel(assistedFactory = VideoUploadingViewModel.Factory::class)
class VideoUploadingViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    @Assisted("player") val player: Player,
    private val getOwnedChannels: GetOwnedChannels,
    private val validateChannel: ValidateChannelId,
    private val validateImage: ValidateImage,
    private val validateVideoSource: ValidateVideoSource,
    private val validateTitle: ValidateTitle,
    private val validateDescription: ValidateDescription,
    private val getVideoMetaData: GetVideoMetaData,
    private val uploadVideo: UploadVideo
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val state = _state.onStart { start() }.stateIn(_state.value)
    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            ScreenAction.Restart -> start()
            is ScreenAction.ChangeChannel -> onChannelChanged(action.channelId)
            is ScreenAction.ChangeTitle -> onTitleChanged(action.title)
            ScreenAction.FocusTitle -> onTitleFocused()
            ScreenAction.BlurTitle -> onTitleBlurred()
            is ScreenAction.ChangeSource -> onSourceChanged(action.source)
            is ScreenAction.ChangeCover -> onCoverChanged(action.cover)
            is ScreenAction.ChangeDescription -> onDescriptionChanged(action.description)
            ScreenAction.FocusDescription -> onDescriptionFocused()
            ScreenAction.BlurDescription -> onDescriptionBlurred()
            ScreenAction.Submit -> upload()
            ScreenAction.Cancel -> viewModelScope.launch {
                _events.emit(ScreenEvent.Cancelled)
            }
            is ScreenAction.ShowPermissionLack -> viewModelScope.launch {
                _events.emit(ScreenEvent.PermissionLacked(action.message))
            }
        }
    }

    private fun start() {
        if (_state.value is ScreenState.Starting) {
            return
        }
        _state.update {
            ScreenState.Starting
        }
        viewModelScope.launch {
            getOwnedChannels(
                userId = userId,
                partIndex = 0,
                partSize = 100
            ).onSuccess { channels -> // TODO all channels fetch here?
                _state.update {
                    ScreenState.Editing(
                        channels = channels.map {
                            ChannelOptionUi(
                                channelId = it.channelId,
                                title = it.title
                            )
                        },
                        video = VideoUploadingInput()
                    )
                }
            }.onFailure { error ->
                _state.update {
                    ScreenState.Failure(error)
                }
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Failure(error))
                }
            }
        }
    }

    private fun onChannelChanged(channelId: Long?) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    channelId = currentState.video.channelId.copy(
                        value = channelId,
                        error = validateChannel(channelId).errorOrNull()
                    )
                )
            ) ?: it
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    title = currentState.video.title.copy(value = title)
                )
            ) ?: it
        }
    }

    private fun onTitleFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    title = currentState.video.title.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onTitleBlurred() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    title = currentState.video.title.copy(
                        error = validateTitle(currentState.video.title.value).errorOrNull()
                    )
                )
            ) ?: it
        }
    }

    private fun onSourceChanged(source: String?) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    source = currentState.video.source.copy(
                        value = source,
                        error = validateVideoSource(source).errorOrNull()
                    )
                )
            ) ?: it
        }
    }

    private fun onCoverChanged(cover: String?) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    cover = currentState.video.cover.copy(
                        value = cover,
                        error = cover?.let {
                            validateImage(it).errorOrNull()
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    description = currentState.video.description.copy(value = description)
                )
            ) ?: it
        }
    }

    private fun onDescriptionFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    description = currentState.video.description.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onDescriptionBlurred() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    description = currentState.video.description.copy(
                        error = validateDescription(currentState.video.description.value).errorOrNull()
                    )
                )
            ) ?: it
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun upload() {
        _state.update {
            val currentState = _state.value as? ScreenState.Editing
            if (currentState == null || currentState.isLoading) {
                return
            }
            currentState.copy(
                video = currentState.video.copy(
                    channelId = currentState.video.channelId.copy(
                        error = validateChannel(currentState.video.channelId.value).errorOrNull()
                    ),
                    title = currentState.video.title.copy(
                        error = validateTitle(currentState.video.title.value).errorOrNull()
                    ),
                    source = currentState.video.source.copy(
                        error = validateVideoSource(currentState.video.source.value).errorOrNull()
                    ),
                    cover = currentState.video.cover.copy(
                        error = currentState.video.cover.value?.let {
                            validateImage(it).errorOrNull()
                        }
                    ),
                    description = currentState.video.description.copy(
                        error = currentState.video.description.value.takeIf { it.isNotEmpty() }
                            ?.let {
                                validateDescription(it).errorOrNull()
                            }
                    )
                )
            )
        }
        val currentState = _state.value as? ScreenState.Editing
        if (
            currentState == null
            || currentState.video.title.error != null
            || currentState.video.channelId.error != null
            || currentState.video.source.error != null
            || currentState.video.description.error != null
            || currentState.video.cover.error != null
        ) {
            return
        }
        val video = currentState.video
        viewModelScope.launch {
            val videoMetaData = getVideoMetaData(video.source.value!!) as Result.Success // TODO: handle failure cases
            _state.update {
                val currentState = _state.value as? ScreenState.Editing
                currentState?.copy(isLoading = true) ?: it
            }
            uploadVideo(
                video = VideoCreationModel(
                    channelId = video.channelId.value!!,
                    description = video.description.value.takeIf { it.isNotEmpty() },
                    title = video.title.value,
                    cover = video.cover.value,
                    metaData = videoMetaData.data
                )
            ).onSuccess { pendingVideo ->
                viewModelScope.launch {
                    _events.emit(
                        ScreenEvent.Success(
                            tmpId = pendingVideo.tmpId,
                            channelId = video.channelId.value,
                            source = currentState.video.source.value
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    val currentState = it as? ScreenState.Editing
                    if (error is VideoUploadingError) {
                        currentState?.copy(
                            video = currentState.video.copy(
                                channelId = currentState.video.channelId.copy(
                                    error = error.channelError
                                ),
                                title = currentState.video.title.copy(
                                    error = error.titleError
                                ),
                                source = currentState.video.source.copy(
                                    error = error.sourceError
                                ),
                                cover = currentState.video.cover.copy(
                                    error = error.coverError
                                ),
                                description = currentState.video.description.copy(
                                    error = error.descriptionError
                                )
                            ),
                            isLoading = false
                        ) ?: it
                    } else {
                        currentState?.copy(isLoading = false) ?: it
                    }
                }
                if (error !is VideoUploadingError) {
                    viewModelScope.launch {
                        _events.emit(ScreenEvent.Failure(error))
                    }
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