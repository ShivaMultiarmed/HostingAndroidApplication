package mikhail.shell.video.hosting.presentation.channel.create

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
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.channels.CreateChannel
import mikhail.shell.video.hosting.domain.utils.isBlank

@HiltViewModel(assistedFactory = CreateChannelViewModel.Factory::class)
class CreateChannelViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val _createChannel: CreateChannel
): ViewModel() {
    private val _state = MutableStateFlow(CreateChannelScreenState())
    val state = _state.asStateFlow()

    fun createChannel(input: ChannelInputState) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        val compoundError = validateChannelInput(input)
        if (compoundError == null) {
            val channel = Channel(
                ownerId = userId,
                description = input.description,
                title = input.title!!,
                alias = input.alias
            )
            viewModelScope.launch {
                _createChannel(
                    channel,
                    input.avatar,
                    input.cover
                ).onSuccess {
                    _state.value = CreateChannelScreenState(
                        channel = it,
                        error = null,
                        isLoading = false
                    )
                }.onFailure {
                    _state.value = CreateChannelScreenState(
                        channel = null,
                        error = it,
                        isLoading = false
                    )
                }
            }
        } else {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = compoundError
                )
            }
        }
    }
    private fun validateChannelInput(input: ChannelInputState): CompoundError<ChannelCreationError>? {
        val error = CompoundError<ChannelCreationError>()
        if (input.title.isBlank())
            error.add(ChannelCreationError.TITLE_EMPTY)
        return if (error.isNotNull())
            error
        else
            null
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): CreateChannelViewModel
    }
}