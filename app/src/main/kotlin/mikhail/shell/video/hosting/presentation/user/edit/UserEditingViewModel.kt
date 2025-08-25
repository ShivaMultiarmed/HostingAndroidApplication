package mikhail.shell.video.hosting.presentation.user.edit

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
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.usecases.user.EditUser
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.domain.usecases.user.RemoveUser

@HiltViewModel(assistedFactory = UserEditingViewModel.Factory::class)
class UserEditingViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val _getUser: GetUser,
    private val _editUser: EditUser,
    private val _removeUser: RemoveUser
) : ViewModel() {
    private val _state = MutableStateFlow<UserEditingScreenState>(UserEditingScreenState.Loading)
    val state = _state
        .onStart {
            load()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = _state.value
        )

    fun onEvent(event: UserEditingUiEvent) {
        when (event) {
            is UserEditingUiEvent.AvatarChanged -> TODO()
            is UserEditingUiEvent.BioChanged -> TODO()
            UserEditingUiEvent.Cancel -> TODO()
            is UserEditingUiEvent.EmailChanged -> TODO()
            is UserEditingUiEvent.NameChanged -> TODO()
            is UserEditingUiEvent.NickChanged -> TODO()
            UserEditingUiEvent.Reload -> TODO()
            UserEditingUiEvent.Remove -> TODO()
            UserEditingUiEvent.Submit -> TODO()
            is UserEditingUiEvent.TelChanged -> TODO()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _getUser(userId).onSuccess { initialUser ->
                _state.update {
                    UserEditingScreenState.Editing(
                        initialUser = initialUser.toEditUi(),
                        editedUser = UserEditingInputState(
                            nick = initialUser.nick,
                            name = initialUser.name ?: "",
                            bio = initialUser.bio ?: "",
                            tel = initialUser.tel ?: "",
                            email = initialUser.email ?: ""
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    UserEditingScreenState.Failure(error)
                }
            }
        }
    }

    private fun edit() {
        _state.update {
            it as UserEditingScreenState.Editing
            it.copy(isLoading = true)
        }
        val input = (_state.value as UserEditingScreenState.Editing).editedUser
        val user = User(
            userId = userId,
            nick = input.nick,
            name = input.name.takeIf { it.isNotEmpty() },
            avatar = input.avatar,
            bio = input.bio.takeIf { it.isNotEmpty() },
            tel = input.tel.takeIf { it.isNotEmpty() }?.substring(1),
            email = input.email.takeIf { it.isNotEmpty() }
        )
        viewModelScope.launch {
            _editUser(
                user = user,
                avatar = input.avatar,
                avatarAction = input.avatarAction
            ).onSuccess { editedUser ->
                _state.update {
                    UserEditingScreenState.Success
                }
            }.onFailure { error ->
                _state.update {
                    it as UserEditingScreenState.Editing
                    it.copy(
                        error = error,
                        isLoading = false
                    )
                }
            }
        }

    }

    private fun remove() {
        _state.update {
            it as UserEditingScreenState.Editing
            it.copy(isRemoving = true)
        }
        viewModelScope.launch {
            _removeUser().onSuccess {
                _state.update {
                    UserEditingScreenState.Removed
                }
            }.onFailure { error ->
                _state.update {
                    it as UserEditingScreenState.Editing
                    it.copy(
                        isRemoving = false,
                        removingError = error
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): UserEditingViewModel
    }
}

sealed class UserEditingUiEvent {
    data class NickChanged(val nick: String): UserEditingUiEvent()
    data class NameChanged(val name: String): UserEditingUiEvent()
    data class AvatarChanged(val avatar: String?, val action: EditAction): UserEditingUiEvent()
    data class BioChanged(val bio: String): UserEditingUiEvent()
    data class TelChanged(val tel: String): UserEditingUiEvent()
    data class EmailChanged(val email: String): UserEditingUiEvent()
    data object Submit: UserEditingUiEvent()
    data object Cancel: UserEditingUiEvent()
    data object Reload: UserEditingUiEvent()
    data object Remove: UserEditingUiEvent()
}