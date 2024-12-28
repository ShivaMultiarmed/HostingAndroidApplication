package mikhail.shell.video.hosting.presentation.subscriptions

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
import mikhail.shell.video.hosting.domain.usecases.channels.LoadSubscriptionChannels

@HiltViewModel(assistedFactory = SubscriptionsScreenViewModel.Factory::class)
class SubscriptionsScreenViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val _loadSubscriptionChannels: LoadSubscriptionChannels
): ViewModel() {
    private val _state = MutableStateFlow(SubscriptionsScreenState())
    val state get() = _state.asStateFlow()
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
            _loadSubscriptionChannels(userId).onSuccess { fetchedChannels ->
                _state.update {
                    it.copy(
                        channels = fetchedChannels,
                        error = null,
                        isLoading = false
                    )
                }
            }.onFailure {err ->
                _state.update {
                    it.copy(
                        channels = null,
                        error = err,
                        isLoading = false
                    )
                }
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): SubscriptionsScreenViewModel
    }
}