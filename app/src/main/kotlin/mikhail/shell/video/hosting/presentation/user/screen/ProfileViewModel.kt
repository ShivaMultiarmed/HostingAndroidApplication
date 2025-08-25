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
import mikhail.shell.video.hosting.domain.usecases.authentication.SignOut
import mikhail.shell.video.hosting.domain.usecases.channels.GetOwnedChannels
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import mikhail.shell.video.hosting.presentation.user.models.toUi

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val getUser: GetUser,
    private val getOwnedChannels: GetOwnedChannels,
    private val signOut: SignOut
) : ViewModel() {
    private val _state = MutableStateFlow<ProfileScreenState>(ProfileScreenState())
    val state = _state.onStart {
        viewModelScope.launch {
            load()
            loadChannels()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = _state.value
    )

    fun onEvent(event: ProfileScreenUiEvent) {
        when(event) {
            ProfileScreenUiEvent.Reload -> viewModelScope.launch { load() }
            ProfileScreenUiEvent.ReloadChannels -> viewModelScope.launch { loadChannels() }
            ProfileScreenUiEvent.SignOut -> signOut()
            else -> Unit
        }
    }

    private suspend fun load() {
        getUser(userId)
            .onSuccess { user ->
                _state.update {
                    it.copy(
                        user = user.toUi(),
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error
                    )
                }
            }
    }

    private suspend fun loadChannels() {
        _state.update {
            it.copy(
                channelState = it.channelState.copy(isLoading = true)
            )
        }
        getOwnedChannels(
            userId = userId,
            partIndex = (_state.value.channelState.channels?.size ?: 0).toLong() / PART_SIZE,
            partSize = PART_SIZE
        ).onSuccess { channels ->
            _state.update {
                it.copy(
                    channelState = it.channelState.copy(
                        channels = channels.map { it.toUi() },
                        error = null,
                        isLoading = false,
                        hasMore = channels.size < PART_SIZE
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
            signOut.invoke().onSuccess {
                _state.update {
                    it.copy(signedOut = true)
                }
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
    data class ClickedChannel(val channelId: Long): ProfileScreenUiEvent()
    data object PublishVideo: ProfileScreenUiEvent()
    data object CreateChannel: ProfileScreenUiEvent()
    data object Reload: ProfileScreenUiEvent()
    data object ReloadChannels: ProfileScreenUiEvent()
    data object SignOut: ProfileScreenUiEvent()
    data object Invite: ProfileScreenUiEvent()
    data object OpenSettings: ProfileScreenUiEvent()
}