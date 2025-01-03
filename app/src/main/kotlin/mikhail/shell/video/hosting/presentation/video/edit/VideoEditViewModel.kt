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
import mikhail.shell.video.hosting.domain.errors.VideoLoadingError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideo
import mikhail.shell.video.hosting.domain.usecases.videos.UpdateVideo
import javax.inject.Inject

@HiltViewModel(assistedFactory = VideoEditViewModel.Factory::class)
class VideoEditViewModel @AssistedInject constructor(
    @Assisted("videoId") private val videoId: Long,
    private val _editVideo: UpdateVideo,
    private val _getVideo: GetVideo
): ViewModel() {
    private val _state = MutableStateFlow(VideoEditScreenState())
    val state
        get() = _state.asStateFlow()

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
                        isLoading = false
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = err
                    )
                }
            }
        }
    }

    fun edit(video: Video, coverAction: EditAction, cover: File?) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _editVideo(
                video,
                coverAction,
                cover
            ).onSuccess { updatedVideo ->
                _state.update {
                    it.copy(
                        updatedVideo = updatedVideo,
                        error = null,
                        isLoading = false
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        updatedVideo = null,
                        error = err,
                        isLoading = false
                    )
                }
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("videoId") videoId: Long): VideoEditViewModel
    }
}