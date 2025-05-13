package mikhail.shell.video.hosting.presentation.video.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannelsByOwner

@HiltViewModel(assistedFactory = UploadVideoViewModel.Factory::class)
class UploadVideoViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    @Assisted("player") val player: Player,
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
                        isLoading = false,
                        error = CompoundError(mutableListOf(ChannelLoadingError.UNEXPECTED))
                    )
                }
            }
        }
    }
    fun validateVideoInput(input: UploadVideoInput): CompoundError<UploadVideoError>? {
        val compoundError = CompoundError<UploadVideoError>()
        if (input.title.isEmpty())
            compoundError.add(UploadVideoError.TITLE_EMPTY)
        if (input.source == null) {
            compoundError.add(UploadVideoError.SOURCE_EMPTY)
        }
        if (input.channelId == null) {
            compoundError.add(UploadVideoError.CHANNEL_NOT_CHOSEN)
        }
        return if (compoundError.isNotNull()) compoundError.also { cerr ->
            _state.update {
                it.copy(
                    video = null,
                    isLoading = false,
                    error = CompoundError(cerr.errors.toMutableList())
                )
            }
        } else null
    }
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: Long,
            @Assisted("player") player: Player
        ): UploadVideoViewModel
    }
}