package mikhail.shell.video.hosting.presentation.user.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.usecases.authentication.SignOut
import mikhail.shell.video.hosting.domain.usecases.channels.GetOwnedChannels
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import mikhail.shell.video.hosting.presentation.user.models.toUi

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val getUser: GetUser,
    private val getOwnedChannels: GetOwnedChannels,
    private val getChannelLogoUrl: GetChannelLogoUrl,
    private val signOut: SignOut
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileScreenState())
    val state = _state.onStart {
        initialize()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = _state.value
    )

    fun onEvent(event: ProfileScreenUiEvent) {
        when (event) {
            ProfileScreenUiEvent.Restart -> viewModelScope.launch { initialize() }
            ProfileScreenUiEvent.ReloadChannels, ProfileScreenUiEvent.EndReached -> viewModelScope.launch { loadChannels(start = false) }
            ProfileScreenUiEvent.SignOut -> signOut()
            else -> Unit
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            load()
            loadChannels(start = true)
        }
    }

    private suspend fun load() {
        _state.update {
            it.copy(
                isStarting = true
            )
        }
        getUser(userId).onSuccess { user ->
            _state.update {
                it.copy(
                    user = user.toUi(),
                    error = null,
                    isStarting = false
                )
            }
        }.onFailure { error ->
            _state.update {
                it.copy(
                    error = error,
                    isStarting = false
                )
            }
        }
    }

    private suspend fun loadChannels(start: Boolean = false) {
        _state.update {
            it.copy(
                channelState = it.channelState.copy(
                    isLoading = true
                )
            )
        }
        getOwnedChannels(
            userId = userId,
            partIndex = if (start) 0 else _state.value.channelState.nextPartIndex,
            partSize = PART_SIZE
        ).onSuccess { channels ->
            _state.update {
                it.copy(
                    channelState = it.channelState.copy(
                        channels = ((if (start) null else _state.value.channelState.channels)?: emptyList()) + channels.map {
                            it.toUi(
                                logo = getChannelLogoUrl(
                                    channelId = it.channelId!!,
                                    size = ImageSize.MEDIUM
                                )
                            )
                        },
                        error = null,
                        isLoading = false,
                        nextPartIndex = (if (start) 0 else _state.value.channelState.nextPartIndex) + 1,
                        hasMore = channels.size == PART_SIZE
                    )
                )
            }
        }.onFailure { error ->
            _state.update {
                it.copy(
                    channelState = it.channelState.copy(
                        error = error,
                        isLoading = false
                    )
                )
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOut.invoke()
            _state.update {
                it.copy(isSignedOut = true)
            }
        }
    }

    private companion object {
        const val PART_SIZE = 10
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): ProfileViewModel
    }
}

sealed class ProfileScreenUiEvent {
    data object Restart : ProfileScreenUiEvent()
    data object ReloadChannels : ProfileScreenUiEvent()
    data object EndReached : ProfileScreenUiEvent()
    data class ClickedChannel(val channelId: Long) : ProfileScreenUiEvent()
    data object OpenSettings : ProfileScreenUiEvent()
    data object PublishVideo : ProfileScreenUiEvent()
    data object CreateChannel : ProfileScreenUiEvent()
    data object SignOut : ProfileScreenUiEvent()
    data object Invite : ProfileScreenUiEvent()
}