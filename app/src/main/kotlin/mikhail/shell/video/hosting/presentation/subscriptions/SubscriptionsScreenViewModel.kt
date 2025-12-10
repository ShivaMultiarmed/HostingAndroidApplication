package mikhail.shell.video.hosting.presentation.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.usecases.channels.GetSubscriptions
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import mikhail.shell.video.hosting.presentation.utils.stateIn
import javax.inject.Inject

@HiltViewModel
class SubscriptionsScreenViewModel @Inject constructor(
    private val getSubscriptions: GetSubscriptions,
    private val getChannelLogoUrl: GetChannelLogoUrl
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionsScreenState())
    val state = _state.onStart { load(start = true) }.stateIn(_state.value)

    private val _events = MutableSharedFlow<SubscriptionsScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: SubscriptionsScreenAction) {
        when (action) {
            SubscriptionsScreenAction.Restart -> load(start = true)
            SubscriptionsScreenAction.LoadNextPart -> load()
            is SubscriptionsScreenAction.ChooseChannel -> viewModelScope.launch {
                _events.emit(SubscriptionsScreenEvent.ChannelChosen(action.channelId))
            }
        }
    }

    private fun load(start: Boolean = false) {
        if (_state.value.isStarting || _state.value.isLoading) {
            return
        }
        _state.update {
            it.copy(
                isStarting = start,
                isLoading = !start
            )
        }
        viewModelScope.launch {
            getSubscriptions(
                partIndex = if (start) 0 else _state.value.nextPartIndex,
                partSize = PART_SIZE
            ).onSuccess { channels ->
                _state.update {
                    it.copy(
                        channels = ((if (start) null else it.channels)?: emptyList()) + channels.map {
                            it.toUi(
                                logo = getChannelLogoUrl(
                                    channelId = it.channelId,
                                    size = ImageSize.MEDIUM
                                )
                            )
                        },
                        error = null,
                        isStarting = false,
                        isLoading = false,
                        nextPartIndex = (if (start) 0 else it.nextPartIndex) + 1,
                        hasMore = channels.size == PART_SIZE
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error,
                        isStarting = false,
                        isLoading = false
                    )
                }
                viewModelScope.launch {
                    _events.emit(SubscriptionsScreenEvent.Failure(error))
                }
            }
        }
    }

    private companion object {
        const val PART_SIZE = 10
    }
}