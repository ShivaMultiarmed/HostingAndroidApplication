package mikhail.shell.video.hosting.presentation.video.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val _state = MutableStateFlow<VideoUploadingScreenState>(VideoUploadingScreenState.Loading)
    val state = _state.onStart { start() }.stateIn(_state.value)

    fun onEvent(event: VideoUploadingScreenUiEvent) {
        viewModelScope.launch {
            when (event) {
                VideoUploadingScreenUiEvent.Restart -> start()
                is VideoUploadingScreenUiEvent.ChannelChanged -> onChannelChanged(event.channelId)
                is VideoUploadingScreenUiEvent.TitleChanged -> onTitleChanged(event.title)
                VideoUploadingScreenUiEvent.TitleFocused -> onTitleFocused()
                VideoUploadingScreenUiEvent.TitleBlurred -> onTitleBlurred()
                is VideoUploadingScreenUiEvent.SourceChanged -> onSourceChanged(event.source)
                is VideoUploadingScreenUiEvent.CoverChanged -> onCoverChanged(event.cover)
                is VideoUploadingScreenUiEvent.DescriptionChanged -> onDescriptionChanged(event.description)
                VideoUploadingScreenUiEvent.DescriptionFocused -> onDescriptionFocused()
                VideoUploadingScreenUiEvent.DescriptionBlurred -> onDescriptionBlurred()
                VideoUploadingScreenUiEvent.Submit -> upload()
                else -> null
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
                    VideoUploadingScreenState.Editing(
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
                    VideoUploadingScreenState.Failure(error)
                }
            }
        }
    }

    private fun onChannelChanged(channelId: Long?) {
        _state.update {
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = it as? VideoUploadingScreenState.Editing
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
            val currentState = _state.value as? VideoUploadingScreenState.Editing
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
        val currentState = _state.value as? VideoUploadingScreenState.Editing
        if (currentState == null) {
            return
        }
        val video = currentState.video
        viewModelScope.launch {
            val videoMetaData = getVideoMetaData(video.source.value!!) as Result.Success // TODO: handle failure cases
            _state.update {
                val currentState = _state.value as? VideoUploadingScreenState.Editing
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
                _state.update {
                    val currentState = it as? VideoUploadingScreenState.Editing
                    if (currentState != null) {
                        VideoUploadingScreenState.Success(
                            uploadId = uploadId,
                            source = currentState.video.source.value!!
                        )
                    } else it
                }
            }.onFailure { error ->
                _state.update {
                    val currentState = it as? VideoUploadingScreenState.Editing
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
                            error = error,
                            isLoading = false
                        )?: it
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

sealed class VideoUploadingScreenUiEvent {
    data object Cancel : VideoUploadingScreenUiEvent()
    data object Restart : VideoUploadingScreenUiEvent()
    data class ChannelChanged(val channelId: Long?) : VideoUploadingScreenUiEvent()
    data class TitleChanged(val title: String) : VideoUploadingScreenUiEvent()
    data object TitleBlurred : VideoUploadingScreenUiEvent()
    data object TitleFocused : VideoUploadingScreenUiEvent()
    data class SourceChanged(val source: String?) : VideoUploadingScreenUiEvent()
    data class CoverChanged(val cover: String?) : VideoUploadingScreenUiEvent()
    data class DescriptionChanged(val description: String) : VideoUploadingScreenUiEvent()
    data object DescriptionBlurred : VideoUploadingScreenUiEvent()
    data object DescriptionFocused : VideoUploadingScreenUiEvent()
    data object Submit : VideoUploadingScreenUiEvent()
}