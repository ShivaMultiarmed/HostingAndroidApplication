package mikhail.shell.video.hosting.presentation.channel

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
import mikhail.shell.video.hosting.domain.usecases.channels.GetExtendedChannelInfo

@HiltViewModel(assistedFactory = ChannelScreenViewModel.Factory::class)
class ChannelScreenViewModel @AssistedInject constructor(
    @Assisted("channelId") private val _channelId: Long,
    @Assisted("userId") private val _userId: Long,
    private val _getExtendedChannelInfo: GetExtendedChannelInfo
) : ViewModel() {
    private val _state = MutableStateFlow(ChannelScreenState())
    val state = _state.asStateFlow()

    init {
        loadChannelInfo()
    }

    fun loadChannelInfo() {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _getExtendedChannelInfo(
                _channelId,
                _userId
            ).onSuccess { chInfo ->
                _state.update {
                    it.copy(
                        info = chInfo,
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("channelId") channelId: Long, @Assisted("userId") userId: Long) : ChannelScreenViewModel
    }
}