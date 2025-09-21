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
import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.usecases.channels.GetSubscriptions
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import javax.inject.Inject

@HiltViewModel
class SubscriptionsScreenViewModel @Inject constructor(
    private val getSubscriptions: GetSubscriptions,
    private val getChannelLogoUrl: GetChannelLogoUrl
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionsScreenState())
    val state = _state.onStart {
        load(start = true)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = _state.value
    )

    fun onEvent(event: SubscriptionsScreenUiEvent) {
        when (event) {
            is SubscriptionsScreenUiEvent.Restart -> load(start = true)
            SubscriptionsScreenUiEvent.EndReached, SubscriptionsScreenUiEvent.Reload -> load()
            else -> Unit
        }
    }

    private fun load(start: Boolean = false) {
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
            ).onSuccess { fetchedChannels ->
                _state.update {
                    it.copy(
                        channels = ((if (start) null else it.channels)?: emptyList()) + fetchedChannels.map {
                            it.toUi(
                                logo = getChannelLogoUrl(
                                    channelId = it.channelId!!,
                                    size = ImageSize.MEDIUM
                                )
                            )
                        },
                        error = null,
                        isStarting = false,
                        isLoading = false,
                        nextPartIndex = (if (start) 0 else it.nextPartIndex) + 1,
                        hasMore = fetchedChannels.size == PART_SIZE
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        error = err,
                        isStarting = false,
                        isLoading = false
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
    data object Restart : SubscriptionsScreenUiEvent()
    data object EndReached : SubscriptionsScreenUiEvent()
    data object Reload: SubscriptionsScreenUiEvent()
    data class ChannelClicked(val channelId: Long) : SubscriptionsScreenUiEvent()
}