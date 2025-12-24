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
import mikhail.shell.video.hosting.domain.errors.video.VideoEditingError
import mikhail.shell.video.hosting.domain.models.EditingAction
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.VideoEditingModel
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.videos.EditVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoCoverUrl
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import mikhail.shell.video.hosting.domain.utils.ValidateTitle
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.stateIn
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreenState as ScreenState

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
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val state = _state.onStart { start() }.stateIn(_state.value)

    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            ScreenAction.Cancel -> viewModelScope.launch {
                _events.emit(ScreenEvent.Cancelled)
            }
            ScreenAction.Restart -> start()
            is ScreenAction.ChangeTitle -> onTitleChanged(action.title)
            ScreenAction.FocusTitle -> onTitleFocused()
            ScreenAction.BlurTitle -> onTitleBlurred()
            is ScreenAction.ChangeCover -> onCoverChanged(action.cover)
            is ScreenAction.ChangeDescription -> onDescriptionChanged(action.description)
            ScreenAction.FocusDescription -> onDescriptionFocused()
            ScreenAction.BlurDescription -> onDescriptionBlurred()
            ScreenAction.Submit -> edit()
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
            getVideo(videoId).onSuccess { video ->
                _state.update {
                    ScreenState.Editing(
                        video = VideoEditingInputState.initialize(
                            videoId = videoId,
                            title = video.title,
                            cover = getVideoCoverUrl(videoId = videoId, size = ImageSize.MEDIUM),
                            description = video.description?: ""
                        )
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

    private fun onCoverChanged(cover: EditingState<String?>) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                video = currentState.video.copy(
                    cover = currentState.video.cover.copy(
                        value = cover,
                        error = when (cover) {
                            is EditingState.Editing -> validateImage(cover.value!!).errorOrNull()
                            else -> null
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

    private fun edit() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            if (currentState == null || currentState.isLoading) {
                return
            }
            currentState.copy(
                video = currentState.video.copy(
                    title = currentState.video.title.copy(
                        error = validateTitle(currentState.video.title.value).errorOrNull()
                    ),
                    cover = currentState.video.cover.copy(
                        error = currentState.video.cover.value.let {
                            when (it) {
                                is EditingState.Editing -> validateImage(it.value!!).errorOrNull()
                                else -> null
                            }
                        }
                    ),
                    description = currentState.video.description.copy(
                        error = validateDescription(currentState.video.description.value).errorOrNull()
                    )
                )
            )
        }
        val currentState = _state.value as? ScreenState.Editing
        if (
            currentState == null
            || currentState.video.title.error != null
            || currentState.video.description.error != null
            || currentState.video.cover.error != null
        ) {
            return
        }
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ScreenState.Editing
                currentState?.copy(isLoading = true) ?: it
            }
            editVideo(
                video = VideoEditingModel(
                    videoId = videoId,
                    title = currentState.video.title.value,
                    description = currentState.video.description.value.takeIf { it.isNotEmpty() },
                    cover = when (currentState.video.cover.value) {
                        is EditingState.Editing -> EditingAction.Edit(currentState.video.cover.value.value!!)
                        is EditingState.Keeping -> EditingAction.Keep
                        is EditingState.Removing -> EditingAction.Remove
                    }
                )
            ).onSuccess { updatedVideo ->
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Success)
                }
            }.onFailure { error ->
                _state.update {
                    val currentState = it as? ScreenState.Editing
                    currentState?.copy(isLoading = false) ?: it
                }
                if (error is VideoEditingError) {
                    _state.update {
                        val currentState = it as? ScreenState.Editing
                        currentState?.copy(
                            video = currentState.video.copy(
                                title = currentState.video.title.copy(
                                    error = error.titleError
                                ),
                                cover = currentState.video.cover.copy(
                                    error = error.coverError
                                ),
                                description = currentState.video.description.copy(
                                    error = error.descriptionError
                                )
                            )
                        ) ?: it
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(ScreenEvent.Failure(error))
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