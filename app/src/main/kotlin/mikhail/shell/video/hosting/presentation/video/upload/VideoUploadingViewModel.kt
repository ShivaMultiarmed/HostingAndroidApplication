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
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.errors.video.VideoUploadingError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoCreationModel
import mikhail.shell.video.hosting.domain.usecases.channels.GetOwnedChannels
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoMetaData
import mikhail.shell.video.hosting.domain.usecases.videos.UploadVideo
import mikhail.shell.video.hosting.domain.usecases.videos.validation.ValidateChannelId
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import mikhail.shell.video.hosting.domain.utils.ValidateTitle
import mikhail.shell.video.hosting.domain.utils.ValidateVideoSource
import mikhail.shell.video.hosting.presentation.utils.stateIn
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.ChannelChanged
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.CoverChanged
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.DescriptionBlurred
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.DescriptionChanged
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.DescriptionFocused
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.Restart
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.SourceChanged
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.Submit
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.TitleBlurred
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.TitleChanged
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.TitleFocused
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenEvent.Success
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState.Editing
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState.Failure
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState.Starting
import kotlin.uuid.ExperimentalUuidApi

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
    private val _state = MutableStateFlow<State>(Starting)
    val state = _state.onStart { start() }.stateIn(_state.value)
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    fun onAction(action: Action) {
        viewModelScope.launch {
            when (action) {
                Restart -> start()
                is ChannelChanged -> onChannelChanged(action.channelId)
                is TitleChanged -> onTitleChanged(action.title)
                TitleFocused -> onTitleFocused()
                TitleBlurred -> onTitleBlurred()
                is SourceChanged -> onSourceChanged(action.source)
                is CoverChanged -> onCoverChanged(action.cover)
                is DescriptionChanged -> onDescriptionChanged(action.description)
                DescriptionFocused -> onDescriptionFocused()
                DescriptionBlurred -> onDescriptionBlurred()
                Submit -> upload()
                VideoUploadingScreenAction.Cancel -> _events.emit(VideoUploadingScreenEvent.NavigateBack)
            }
        }
    }

    private fun start() {
        viewModelScope.launch {
            getOwnedChannels(
                userId = userId,
                partIndex = 0,
                partSize = 100
            ).onSuccess { channels -> // TODO all channels fetch here?
                _state.update {
                    Editing(
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
                    Failure(error)
                }
            }
        }
    }

    private fun onChannelChanged(channelId: Long?) {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    channelId = currentState.video.channelId.copy(
                        value = channelId,
                        error = channelId.let {
                            val validationResult = validateChannel(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            )?: it
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    title = currentState.video.title.copy(
                        value = title
                    )
                )
            )?: it
        }
    }

    private fun onTitleFocused() {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    title = currentState.video.title.copy(
                        error = null
                    )
                )
            )?: it
        }
    }

    private fun onTitleBlurred() {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    title = currentState.video.title.copy(
                        error = currentState.video.title.value.let {
                            val validationResult = validateTitle(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            )?: it
        }
    }

    private fun onSourceChanged(source: String?) {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    source = currentState.video.source.copy(
                        value = source,
                        error = source.let {
                            val validationResult = validateVideoSource(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            )?: it
        }
    }

    private fun onCoverChanged(cover: String?) {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    cover = currentState.video.cover.copy(
                        value = cover,
                        error = cover?.let {
                            val validationResult = validateImage(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            )?: it
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    description = currentState.video.description.copy(
                        value = description
                    )
                )
            )?: it
        }
    }

    private fun onDescriptionFocused() {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    description = currentState.video.description.copy(
                        error = null
                    )
                )
            )?: it
        }
    }

    private fun onDescriptionBlurred() {
        _state.update {
            val currentState = it as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    description = currentState.video.description.copy(
                        error = currentState.video.description.value.let {
                            val validationResult = validateDescription(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            )?: it
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun upload() {
        _state.update {
            val currentState = _state.value as? Editing
            currentState?.copy(
                video = currentState.video.copy(
                    channelId = currentState.video.channelId.copy(
                        error = currentState.video.channelId.value.let {
                            val validationResult = validateChannel(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    title = currentState.video.title.copy(
                        error = currentState.video.title.value.let {
                            val validationResult = validateTitle(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    source = currentState.video.source.copy(
                        error = currentState.video.source.value.let {
                            val validationResult = validateVideoSource(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    cover = currentState.video.cover.copy(
                        error = currentState.video.cover.value?.let {
                            val validationResult = validateImage(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    description = currentState.video.description.copy(
                        error = currentState.video.description.value.let {
                            val validationResult = validateDescription(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )

            )?: it
        }
        val currentState = _state.value as? Editing
        if (currentState == null) {
            return
        }
        val video = currentState.video
        viewModelScope.launch {
            val videoMetaData = getVideoMetaData(video.source.value!!) as Result.Success // TODO: handle failure cases
            _state.update {
                val currentState = _state.value as? Editing
                currentState?.copy(
                    isLoading = true
                )?: it
            }
            uploadVideo(
                video = VideoCreationModel(
                    channelId = video.channelId.value!!,
                    description = video.description.value.takeIf { it.isNotEmpty() },
                    title = video.title.value,
                    cover = video.cover.value,
                    metaData = videoMetaData.data
                )
            ).onSuccess { uploadId ->
                viewModelScope.launch {
                    _events.emit(
                        Success(
                            tmpId = uploadId,
                            source = currentState.video.source.value
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    val currentState = it as? Editing
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
                        )?: it
                    } else {
                        currentState?.copy(
                            isLoading = false
                        )?: it
                    }
                }
                if (error !is VideoUploadingError) {
                    viewModelScope.launch {
                        if (error == NetworkError.AUTHENTICATION) {
                            _events.emit(VideoUploadingScreenEvent.RequireAuthentication)
                        } else {
                            _events.emit(VideoUploadingScreenEvent.Failure(error))
                        }
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

