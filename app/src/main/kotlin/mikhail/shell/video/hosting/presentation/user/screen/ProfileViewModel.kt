package mikhail.shell.video.hosting.presentation.user.screen

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
import mikhail.shell.video.hosting.domain.usecases.authentication.SignOut
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannelsByOwner
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.presentation.user.toModel

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val _getUser: GetUser,
    private val _getChannelsByOwner: GetChannelsByOwner,
    private val _signOut: SignOut
): ViewModel() {
    private val _state = MutableStateFlow(ProfileScreenState())
    val state = _state.asStateFlow()
    init {
        loadProfile()
        loadChannels()
    }
    fun loadProfile() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            _getUser(userId).onSuccess {  user ->
                val userModel = user.toModel()
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = userModel,
                        userError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        userError = error
                    )
                }
            }
        }
    }
    fun loadChannels() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            _getChannelsByOwner(userId).onSuccess { gotList ->
                _state.update {
                    it.copy(
                        channelError = null,
                        channels = gotList,
                        isLoading = false
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        channelError = err,
                        channels = null,
                        isLoading = false
                    )
                }
            }
        }
    }
    fun signOut() {
        viewModelScope.launch {
            _signOut(userId)
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): ProfileViewModel
    }
}