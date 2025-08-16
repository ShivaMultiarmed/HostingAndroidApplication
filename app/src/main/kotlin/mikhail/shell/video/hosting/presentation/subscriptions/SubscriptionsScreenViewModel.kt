package mikhail.shell.video.hosting.presentation.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.channels.LoadSubscriptionChannels
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import javax.inject.Inject

@HiltViewModel
class SubscriptionsScreenViewModel @Inject constructor(
    private val _loadSubscriptionChannels: LoadSubscriptionChannels
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionsScreenState())
    val state get() = _state.asStateFlow()

    init {
        loadChannels()
    }

    fun loadChannels() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            _loadSubscriptionChannels()
                .onSuccess { fetchedChannels ->
                    _state.update {
                        it.copy(
                            channels = fetchedChannels.map { it.toUi() },
                            error = null,
                            isLoading = false
                        )
                    }
                }.onFailure { err ->
                    _state.update {
                        it.copy(
                            error = err,
                            isLoading = false
                        )
                    }
                }
        }
    }
}