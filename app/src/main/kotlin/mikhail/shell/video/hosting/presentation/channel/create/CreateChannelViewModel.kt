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
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.usecases.channels.CreateChannel
import javax.inject.Inject

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
        viewModelScope.launch {
            _createChannel(input, userId).onSuccess {
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
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): CreateChannelViewModel
    }
}