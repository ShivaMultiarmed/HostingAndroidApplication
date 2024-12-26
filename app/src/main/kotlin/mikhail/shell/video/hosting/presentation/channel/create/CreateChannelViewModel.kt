package mikhail.shell.video.hosting.presentation.channel.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.usecases.channels.CreateChannel
import javax.inject.Inject

@HiltViewModel
class CreateChannelViewModel @Inject constructor(
    private val userDetailsProvider: UserDetailsProvider,
    private val _createChannel: CreateChannel
): ViewModel() {
    private val userId = userDetailsProvider.getUserId()
    private val _state = MutableStateFlow(CreateChannelScreenState())
    val state = _state.asStateFlow()

    fun createChannel(input: ChannelInputState) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        val channel = Channel(
            ownerId = userId,
            title = input.title,
            alias = input.alias,
            description = input.description
        )
        viewModelScope.launch {
            _createChannel(channel).onSuccess {
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
}