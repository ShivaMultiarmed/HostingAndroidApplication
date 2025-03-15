package mikhail.shell.video.hosting.presentation.channel.edit

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
import mikhail.shell.video.hosting.domain.usecases.EditChannel
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannel
import mikhail.shell.video.hosting.domain.utils.isBlank

@HiltViewModel(assistedFactory = EditChannelViewModel.Factory::class)
class EditChannelViewModel @AssistedInject constructor(
    @Assisted("channelId") private val channelId: Long,
    private val _getChannel: GetChannel,
    private val _editChannel: EditChannel
): ViewModel() {
    private val _state = MutableStateFlow(EditChannelScreenState())
    val state = _state.asStateFlow()
    init {
        loadInitialChannel()
    }
    fun loadInitialChannel() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            _getChannel(channelId).onSuccess { initialChannel ->
                _state.update {
                    it.copy(
                        initialChannel = initialChannel,
                        error = null,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        initialChannel = null,
                        error = error,
                        isLoading = false
                    )
                }
            }
        }
    }
    private fun validateEditedChannel(inputState: EditChannelInputState): CompoundError<Error>? {
        val compoundError = CompoundError<Error>()
        if (inputState.title.isBlank()) {
            compoundError.add(EditChannelError.TITLE_EMPTY)
        }
        return if (compoundError.isNotNull()) {
            compoundError
        } else {
            null
        }
    }
    fun editChannel(inputState: EditChannelInputState) {
        _state.update {
            it.copy(isLoading = true)
        }
        val error = validateEditedChannel(inputState)
        if (error != null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = error
                )
            }
        } else {
            viewModelScope.launch {
                val channel = _state.value.initialChannel!!.copy(
                    title = inputState.title!!,
                    alias = inputState.alias,
                    description = inputState.description,
                    coverUrl = inputState.cover,
                    avatarUrl = inputState.avatar
                )
                _editChannel(
                    channel,
                    inputState.editCoverAction,
                    inputState.editAvatarAction
                ).onSuccess { editedChannel ->
                    _state.update {
                        it.copy(
                            editedChannel = editedChannel,
                            error = null,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            editedChannel = null,
                            error = error,
                            isLoading = false
                        )
                    }
                }
            }
        }

    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("channelId") channelId: Long): EditChannelViewModel
    }
}