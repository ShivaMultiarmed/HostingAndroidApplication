package mikhail.shell.video.hosting.presentation.video.upload

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
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannelsByOwner
import mikhail.shell.video.hosting.domain.usecases.videos.UploadVideo

@HiltViewModel(assistedFactory = UploadVideoViewModel.Factory::class)
class UploadVideoViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val _uploadVideo: UploadVideo,
    private val _getChannelsByOwner: GetChannelsByOwner
): ViewModel() {
    private val _state = MutableStateFlow(UploadVideoScreenState())
    val state = _state.asStateFlow()
    init {
        loadChannels()
    }
    fun loadChannels() {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _getChannelsByOwner(userId).onSuccess {  fetchedList ->
                _state.update {
                    it.copy(
                        channels = fetchedList,
                        isLoading = false
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }
    fun uploadVideo(input: UploadVideoInput) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        val compoundError = validateVideoInput(input)
        if (compoundError == null) {
            viewModelScope.launch {
                _uploadVideo(
                    video = Video(
                        channelId = input.channelId?: 0,
                        title = input.title
                    ),
                    source = input.source!!,
                    cover = input.cover,
                ).onSuccess { vid ->
                    _state.update {
                        it.copy(
                            video = vid,
                            isLoading = false,
                            error = null
                        )
                    }
                }.onFailure { err ->
                    _state.update {
                        it.copy(
                            video = null,
                            isLoading = false,
                            error = err
                        )
                    }
                }
            }
        } else {
            _state.update {
                it.copy(
                    error = compoundError,
                    isLoading = false
                )
            }
        }
    }
    private fun validateVideoInput(input: UploadVideoInput): CompoundError<UploadVideoError>? {
        val compoundError = CompoundError<UploadVideoError>()
        if (input.title.isEmpty())
            compoundError.add(UploadVideoError.TITLE_EMPTY)
        if (input.source?.name == null || input.source.content == null) {
            compoundError.add(UploadVideoError.SOURCE_EMPTY)
        }
        if (input.channelId == null) {
            compoundError.add(UploadVideoError.CHANNEL_NOT_CHOSEN)
        }
        return if (compoundError.isNotNull()) compoundError else null
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): UploadVideoViewModel
    }
}