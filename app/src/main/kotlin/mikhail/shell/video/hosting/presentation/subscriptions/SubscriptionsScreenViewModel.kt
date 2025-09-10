package mikhail.shell.video.hosting.presentation.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.channels.GetSubscriptions
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import javax.inject.Inject

@HiltViewModel
class SubscriptionsScreenViewModel @Inject constructor(
    private val getSubscriptions: GetSubscriptions
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionsScreenState())
    val state = _state.onStart {
        load()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = _state.value
    )

    fun onEvent(event: SubscriptionsScreenUiEvent) {
        when (event) {
            is SubscriptionsScreenUiEvent.Restart -> {
                _state.update {
                    SubscriptionsScreenState()
                }
                load()
            }
            SubscriptionsScreenUiEvent.ReachedBottom -> load()
            else -> Unit
        }
    }

    private fun load() {
        _state.update {
            it.copy(
                isStarting = it.nextPartIndex == 0L,
                isLoading = it.nextPartIndex > 0L
            )
        }
        viewModelScope.launch {
            getSubscriptions(
                partIndex = _state.value.nextPartIndex,
                partSize = PART_SIZE
            ).onSuccess { fetchedChannels ->
                _state.update {
                    it.copy(
                        channels = (it.channels?: emptyList()) + fetchedChannels.map { it.toUi() },
                        error = null,
                        isLoading = false,
                        isStarting = false,
                        nextPartIndex = it.nextPartIndex + 1,
                        hasMore = fetchedChannels.size == PART_SIZE
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        error = err,
                        isLoading = false,
                        isStarting = false
                    )
                }
            }
        }
    }

    private companion object {
        const val PART_SIZE = 10
    }
}

sealed class SubscriptionsScreenUiEvent {
    data object ReachedBottom : SubscriptionsScreenUiEvent()
    data class ClickedChannel(val channelId: Long) : SubscriptionsScreenUiEvent()
    data object Restart : SubscriptionsScreenUiEvent()
}