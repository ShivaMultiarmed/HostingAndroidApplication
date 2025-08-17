package mikhail.shell.video.hosting.presentation.video.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.isNotEmpty
import mikhail.shell.video.hosting.domain.errors.video.VideoEditingError
import mikhail.shell.video.hosting.domain.errors.video.VideoEditingError.TITLE_EMPTY
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.usecases.videos.EditVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideo

@HiltViewModel(assistedFactory = EditVideoViewModel.Factory::class)
class EditVideoViewModel @AssistedInject constructor(
    @Assisted("videoId") private val videoId: Long,
    private val _getVideo: GetVideo,
    private val _editVideo: EditVideo
): ViewModel() {
    private val _state = MutableStateFlow(EditVideoScreenState())
    val state = _state.asStateFlow()

    init {
        loadInitialVideo()
    }

    fun loadInitialVideo() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            _getVideo(videoId).onSuccess { initialVideo ->
                _state.update {
                    it.copy(
                        initialVideo = EditVideoUi(
                            videoId = videoId,
                            title = initialVideo.title,
                            cover = initialVideo.cover,
                            channelId = initialVideo.channelId
                        ),
                        initialVideoError = null,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        initialVideoError = error,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun edit(input: VideoEditInputState) {
        _state.update {
            it.copy(isLoading = true)
        }
        val error = validate(input)
        if (error.isNotEmpty()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    editVideoError = error
                )
            }
        } else {
            val video = Video(
                videoId = videoId,
                channelId = _state.value.initialVideo!!.channelId,
                title = input.title
            )
            viewModelScope.launch {
                _editVideo(
                    video = video,
                    coverAction = input.coverAction,
                    cover = input.cover
                ).onSuccess { updatedVideo ->
                    _state.update {
                        it.copy(
                            editConfirmed = true,
                            editVideoError = null,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            editVideoError = error,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
    private fun validate(input: VideoEditInputState): Error {
        val error = CompoundError<VideoEditingError>()
        if (input.title.isEmpty()) {
            error.add(TITLE_EMPTY)
        }
        return error
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("videoId") videoId: Long): EditVideoViewModel
    }
}