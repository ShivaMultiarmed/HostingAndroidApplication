package mikhail.shell.video.hosting.presentation.video.edit

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
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateImage
import mikhail.shell.video.hosting.domain.usecases.videos.EditVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideo
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateTitle

@HiltViewModel(assistedFactory = VideoEditingViewModel.Factory::class)
class VideoEditingViewModel @AssistedInject constructor(
    @Assisted("videoId") private val videoId: Long,
    private val getVideo: GetVideo,
    private val validateTitle: ValidateTitle,
    private val validateDescription: ValidateDescription,
    private val validateImage: ValidateImage,
    private val editVideo: EditVideo
) : ViewModel() {

    private val _state = MutableStateFlow<VideoEditingScreenState>(VideoEditingScreenState.Loading)
    val state = _state.onStart {
        load()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = _state.value
    )

    fun onEvent(event: VideoEditingUiEvent) {
        when (event) {
            is VideoEditingUiEvent.CoverChanged -> onCoverChanged(event.cover)
            VideoEditingUiEvent.Reload -> load()
            VideoEditingUiEvent.Submit -> edit()
            is VideoEditingUiEvent.TitleChanged -> onTitleChanged(event.title)
            is VideoEditingUiEvent.DescriptionChanged -> onDescriptionChanged(event.description)
            else -> Unit
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            it as VideoEditingScreenState.Editing
            it.copy(
                currentVideo = it.currentVideo.copy(
                    description = description,
                    descriptionError = description.let {
                        val validationResult = validateDescription(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            it as VideoEditingScreenState.Editing
            it.copy(
                currentVideo = it.currentVideo.copy(
                    title = title,
                    titleError = title.let {
                        val validationResult = validateTitle(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onCoverChanged(cover: String?) {
        _state.update {
            it as VideoEditingScreenState.Editing
            it.copy(
                currentVideo = it.currentVideo.copy(
                    cover = cover,
                    coverError = cover?.let {
                        val validationResult = validateImage(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            getVideo(videoId)
                .onSuccess { video ->
                    _state.update {
                        VideoEditingScreenState.Editing(
                            initialVideo = EditableVideoUi(
                                videoId = videoId,
                                title = video.title,
                                cover = video.cover,
                                channelId = video.channelId,
                                description = video.description ?: ""
                            ),
                            currentVideo = VideoEditingInputState(
                                title = video.title,
                                description = video.description ?: ""
                            )
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        VideoEditingScreenState.Failure(error)
                    }
                }
        }
    }

    private fun edit() {
        _state.update {
            it as VideoEditingScreenState.Editing
            it.copy(isLoading = true)
        }
        val currentState = _state.value as VideoEditingScreenState.Editing
        val video = Video(
            videoId = videoId,
            channelId = currentState.initialVideo.channelId,
            title = currentState.currentVideo.title,
            description = currentState.currentVideo.description
        )
        viewModelScope.launch {
            editVideo(
                video = video,
                coverAction = currentState.currentVideo.coverAction,
                cover = currentState.currentVideo.cover
            ).onSuccess { updatedVideo ->
                _state.update {
                    VideoEditingScreenState.Success
                }
            }.onFailure { error ->
                _state.update {
                    it as VideoEditingScreenState.Editing
                    it.copy(
                        submitError = error,
                        isLoading = false
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("videoId") videoId: Long): VideoEditingViewModel
    }
}

sealed class VideoEditingScreenState {
    data object Loading : VideoEditingScreenState()
    data class Editing(
        val initialVideo: EditableVideoUi,
        val currentVideo: VideoEditingInputState,
        val isLoading: Boolean = false,
        val submitError: Error? = null
    ) : VideoEditingScreenState()

    data class Failure(val error: Error) : VideoEditingScreenState()
    data object Success : VideoEditingScreenState()
}

sealed class VideoEditingUiEvent {
    data object Reload : VideoEditingUiEvent()
    data class TitleChanged(val title: String) : VideoEditingUiEvent()
    data class CoverChanged(val cover: String?, val action: EditAction) : VideoEditingUiEvent()
    data class DescriptionChanged(val description: String) : VideoEditingUiEvent()
    data object Submit : VideoEditingUiEvent()
    data object Cancel : VideoEditingUiEvent()
}