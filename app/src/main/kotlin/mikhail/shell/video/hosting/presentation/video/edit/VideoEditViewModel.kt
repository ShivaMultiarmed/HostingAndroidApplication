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
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideo
import mikhail.shell.video.hosting.domain.usecases.videos.UpdateVideo

@HiltViewModel(assistedFactory = VideoEditViewModel.Factory::class)
class VideoEditViewModel @AssistedInject constructor(
    @Assisted("videoId") private val videoId: Long,
    private val _editVideo: UpdateVideo,
    private val _getVideo: GetVideo
): ViewModel() {
    private val _state = MutableStateFlow(VideoEditScreenState())
    val state = _state.asStateFlow()

    init {
        loadInitialVideo()
    }

    fun loadInitialVideo() {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _getVideo(videoId).onSuccess { initialVideo ->
                _state.update {
                    it.copy(
                        initialVideo = initialVideo,
                        initialVideoError = null,
                        isLoading = false
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        initialVideoError = err,
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
                    updateVideoError = error
                )
            }
        } else {
            val video = _state.value.initialVideo!!.copy(title = input.title)
            viewModelScope.launch {
                _editVideo(
                    video,
                    input.coverAction,
                    input.cover
                ).onSuccess { updatedVideo ->
                    _state.update {
                        it.copy(
                            updatedVideo = updatedVideo,
                            updateVideoError = null,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            updateVideoError = error,
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
        fun create(@Assisted("videoId") videoId: Long): VideoEditViewModel
    }
}