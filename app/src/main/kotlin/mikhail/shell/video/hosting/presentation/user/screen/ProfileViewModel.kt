package mikhail.shell.video.hosting.presentation.user.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.ImageSize.MEDIUM
import mikhail.shell.video.hosting.domain.usecases.authentication.SignOut
import mikhail.shell.video.hosting.domain.usecases.channels.GetOwnedChannels
import mikhail.shell.video.hosting.domain.usecases.user.ConstructAvatarUrl
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.channel.models.toUi
import mikhail.shell.video.hosting.presentation.user.models.toUi
import mikhail.shell.video.hosting.presentation.utils.stateIn

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val getUser: GetUser,
    private val constructAvatarUrl: ConstructAvatarUrl,
    private val getOwnedChannels: GetOwnedChannels,
    private val getChannelLogoUrl: GetChannelLogoUrl,
    private val signOut: SignOut
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileScreenState())
    val state = _state.onStart { startAll() }.stateIn( _state.value)

    private val _events = MutableSharedFlow<ProfileScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ProfileScreenAction) {
        when (action) {
            ProfileScreenAction.RestartProfile -> viewModelScope.launch { startAll() }
            ProfileScreenAction.LoadNextChannelsPart -> viewModelScope.launch { loadChannels(start = false) }
            ProfileScreenAction.SignOut -> signOut()
            is ProfileScreenAction.ChooseChannel -> viewModelScope.launch {
                _events.emit(ProfileScreenEvent.ChannelChosen(action.channelId))
            }
            ProfileScreenAction.CreateChannel -> viewModelScope.launch {
                _events.emit(ProfileScreenEvent.ChannelCreationRequested)
            }
            ProfileScreenAction.Invite -> viewModelScope.launch {
                _events.emit(ProfileScreenEvent.InvitationRequested)
            }
            ProfileScreenAction.OpenSettings -> viewModelScope.launch {
                _events.emit(ProfileScreenEvent.SettingsRequested)
            }
            ProfileScreenAction.PublishVideo -> viewModelScope.launch {
                _events.emit(ProfileScreenEvent.VideoUploadingRequested)
            }
        }
    }

    private fun startAll() {
        viewModelScope.launch {
            start()
        }
        viewModelScope.launch {
            loadChannels(start = true)
        }
    }

    private suspend fun start() {
        if (_state.value.isStarting) {
            return
        }
        _state.update {
            it.copy(isStarting = true)
        }
        getUser(userId).onSuccess { user ->
            _state.update {
                it.copy(
                    user = user.toUi(
                        avatar = constructAvatarUrl(userId, MEDIUM)
                    ),
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
            viewModelScope.launch {
                _events.emit(ProfileScreenEvent.Failure(error))
            }
        }
    }

    private suspend fun loadChannels(start: Boolean = false) {
        if (_state.value.channelState.isLoading) {
            return
        }
        _state.update {
            it.copy(
                channelState = it.channelState.copy(isLoading = true)
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
                                    channelId = it.channelId,
                                    size = MEDIUM
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
            viewModelScope.launch {
                _events.emit(ProfileScreenEvent.Failure(error))
            }
        }
    }

    private fun signOut() {
        if (_state.value.isSigningOut) {
            return
        }
        viewModelScope.launch {
            signOut.invoke()
            _events.emit(ProfileScreenEvent.SignedOut)
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