package mikhail.shell.video.hosting.presentation.video.edit

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
import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.errors.video.VideoEditingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoEditingModel
import mikhail.shell.video.hosting.domain.usecases.videos.EditVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoCoverUrl
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import mikhail.shell.video.hosting.domain.utils.ValidateTitle
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.utils.stateIn

@HiltViewModel(assistedFactory = VideoEditingViewModel.Factory::class)
class VideoEditingViewModel @AssistedInject constructor(
    @Assisted("videoId") private val videoId: Long,
    private val getVideo: GetVideo,
    private val validateTitle: ValidateTitle,
    private val validateDescription: ValidateDescription,
    private val getVideoCoverUrl: GetVideoCoverUrl,
    private val validateImage: ValidateImage,
    private val editVideo: EditVideo
) : ViewModel() {
    private val _state = MutableStateFlow<VideoEditingScreenState>(VideoEditingScreenState.Starting)
    val state = _state.onStart { start() }.stateIn(initialValue = _state.value)

    private val _events = MutableSharedFlow<VideoEditingEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: VideoEditingAction) {
        when (action) {
            VideoEditingAction.Cancel -> viewModelScope.launch { _events.emit(VideoEditingEvent.NavigateBack) }
            VideoEditingAction.Restart -> start()
            is VideoEditingAction.TitleChanged -> onTitleChanged(action.title)
            VideoEditingAction.TitleFocused -> onTitleFocused()
            VideoEditingAction.TitleBlurred -> onTitleBlurred()
            is VideoEditingAction.CoverChanged -> onCoverChanged(action.cover, action.action)
            is VideoEditingAction.DescriptionChanged -> onDescriptionChanged(action.description)
            VideoEditingAction.DescriptionFocused -> onDescriptionFocused()
            VideoEditingAction.DescriptionBlurred -> onDescriptionBlurred()
            VideoEditingAction.Submit -> edit()
        }
    }

    private fun start() {
        viewModelScope.launch {
            getVideo(videoId).onSuccess { video ->
                _state.update {
                    VideoEditingScreenState.Editing(
                        initialVideo = EditableVideoUi(
                            videoId = videoId,
                            title = video.title,
                            cover = getVideoCoverUrl(
                                videoId = videoId,
                                size = ImageSize.MEDIUM
                            ),
                            description = video.description ?: ""
                        ),
                        currentVideo = VideoEditingInputState(
                            title = FieldState(value = video.title),
                            cover = FieldState(value = null),
                            description = FieldState(value = video.description ?: "")
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    VideoEditingScreenState.Failure(error)
                }
                val eventToEmit = when (error) {
                    NetworkError.AUTHENTICATION -> VideoEditingEvent.RequireAuthentication
                    NetworkError.NOT_FOUND -> VideoEditingEvent.NavigateBack
                    else -> VideoEditingEvent.Failure(error)
                }
                viewModelScope.launch {
                    _events.emit(eventToEmit)
                }
            }
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            val currentState = it as? VideoEditingScreenState.Editing
            currentState?.copy(
                currentVideo = currentState.currentVideo.copy(
                    title = currentState.currentVideo.title.copy(
                        value = title
                    )
                )
            ) ?: it
        }
    }

    private fun onTitleFocused() {
        _state.update {
            val currentState = it as? VideoEditingScreenState.Editing
            currentState?.copy(
                currentVideo = currentState.currentVideo.copy(
                    title = currentState.currentVideo.title.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onTitleBlurred() {
        _state.update {
            val currentState = it as? VideoEditingScreenState.Editing
            currentState?.copy(
                currentVideo = currentState.currentVideo.copy(
                    title = currentState.currentVideo.title.copy(
                        error = currentState.currentVideo.title.value.let {
                            val validationResult = validateTitle(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onCoverChanged(cover: String?, action: EditAction) {
        _state.update {
            val currentState = it as? VideoEditingScreenState.Editing
            currentState?.copy(
                currentVideo = currentState.currentVideo.copy(
                    cover = currentState.currentVideo.cover.copy(
                        value = cover,
                        error = cover?.let {
                            val validationResult = validateImage(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    coverAction = action
                )
            ) ?: it
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            val currentState = it as? VideoEditingScreenState.Editing
            currentState?.copy(
                currentVideo = currentState.currentVideo.copy(
                    description = currentState.currentVideo.description.copy(
                        value = description
                    )
                )
            ) ?: it
        }
    }

    private fun onDescriptionFocused() {
        _state.update {
            val currentState = it as? VideoEditingScreenState.Editing
            currentState?.copy(
                currentVideo = currentState.currentVideo.copy(
                    description = currentState.currentVideo.description.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onDescriptionBlurred() {
        _state.update {
            val currentState = it as? VideoEditingScreenState.Editing
            currentState?.copy(
                currentVideo = currentState.currentVideo.copy(
                    description = currentState.currentVideo.description.copy(
                        error = currentState.currentVideo.description.value.let {
                            val validationResult = validateDescription(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun edit() {
        _state.update {
            val currentState = it as? VideoEditingScreenState.Editing
            currentState?.copy(
                currentVideo = currentState.currentVideo.copy(
                    title = currentState.currentVideo.title.copy(
                        error = currentState.currentVideo.title.value.let {
                            val validationResult = validateTitle(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    cover = currentState.currentVideo.cover.copy(
                        error = currentState.currentVideo.cover.value?.let {
                            val validationResult = validateImage(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    description = currentState.currentVideo.description.copy(
                        error = currentState.currentVideo.description.value.let {
                            val validationResult = validateDescription(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            ) ?: it
        }
        val currentState = _state.value as? VideoEditingScreenState.Editing
        if (
            currentState == null
            || currentState.currentVideo.title.error != null
            || currentState.currentVideo.description.error != null
            || currentState.currentVideo.cover.error != null
        ) {
            return
        }
        viewModelScope.launch {
            _state.update {
                val currentState = it as? VideoEditingScreenState.Editing
                currentState?.copy(isLoading = true) ?: it
            }
            editVideo(
                video = VideoEditingModel(
                    videoId = videoId,
                    title = currentState.currentVideo.title.value,
                    description = currentState.currentVideo.description.value.takeIf { it.isNotEmpty() },
                    cover = currentState.currentVideo.cover.value,
                    coverAction = currentState.currentVideo.coverAction
                )
            ).onSuccess { updatedVideo ->
                viewModelScope.launch {
                    _events.emit(VideoEditingEvent.Success)
                }
            }.onFailure { error ->
                _state.update {
                    val currentState = it as? VideoEditingScreenState.Editing
                    currentState?.copy(isLoading = false) ?: it
                }
                if (error is VideoEditingError) {
                    _state.update {
                        val currentState = it as? VideoEditingScreenState.Editing
                        currentState?.copy(
                            currentVideo = currentState.currentVideo.copy(
                                title = currentState.currentVideo.title.copy(
                                    error = error.titleError
                                ),
                                cover = currentState.currentVideo.cover.copy(
                                    error = error.coverError
                                ),
                                description = currentState.currentVideo.description.copy(
                                    error = error.descriptionError
                                )
                            )
                        ) ?: it
                    }
                } else {
                    viewModelScope.launch {
                        val eventToEmit = when (error) {
                            NetworkError.AUTHENTICATION -> VideoEditingEvent.RequireAuthentication
                            NetworkError.NOT_FOUND -> VideoEditingEvent.NavigateBack
                            else -> VideoEditingEvent.Failure(error)
                        }
                        _events.emit(eventToEmit)
                    }
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
    data object Starting : VideoEditingScreenState()
    data class Editing(
        val initialVideo: EditableVideoUi,
        val currentVideo: VideoEditingInputState,
        val isLoading: Boolean = false
    ) : VideoEditingScreenState()

    data class Failure(val error: Error) : VideoEditingScreenState()
}

sealed class VideoEditingEvent {
    data object NavigateBack : VideoEditingEvent()
    data object RequireAuthentication : VideoEditingEvent()
    data class Failure(val error: Error) : VideoEditingEvent()
    data object Success : VideoEditingEvent()
}

sealed class VideoEditingAction {
    data object Restart : VideoEditingAction()
    data object TitleFocused : VideoEditingAction()
    data object TitleBlurred : VideoEditingAction()
    data class TitleChanged(val title: String) : VideoEditingAction()
    data class CoverChanged(val cover: String?, val action: EditAction) : VideoEditingAction()
    data object DescriptionFocused : VideoEditingAction()
    data object DescriptionBlurred : VideoEditingAction()
    data class DescriptionChanged(val description: String) : VideoEditingAction()
    data object Submit : VideoEditingAction()
    data object Cancel : VideoEditingAction()
}