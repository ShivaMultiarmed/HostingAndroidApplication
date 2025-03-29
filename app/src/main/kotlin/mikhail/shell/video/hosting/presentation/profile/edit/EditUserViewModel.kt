package mikhail.shell.video.hosting.presentation.profile.edit

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
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.usecases.user.EditUser
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.domain.usecases.user.RemoveUser

@HiltViewModel(assistedFactory = EditUserViewModel.Factory::class)
class EditUserViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val _getUser: GetUser,
    private val _editUser: EditUser,
    private val _removeUser: RemoveUser
) : ViewModel() {
    private val _state = MutableStateFlow(EditUserScreenState())
    val state = _state.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        _state.update {
            it.copy(isInitializing = true)
        }
        viewModelScope.launch {
            _getUser(userId).onSuccess { initialUser ->
                _state.update {
                    it.copy(
                        initialUser = initialUser,
                        getUserError = null,
                        isInitializing = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        getUserError = error,
                        isInitializing = false
                    )
                }
            }
        }
    }

    fun editUser(input: EditUserInputState) {
        _state.update {
            it.copy(
                isEditing = true
            )
        }
        val error = validateUserInput(input)
        if (error != null) {
            _state.update {
                it.copy(
                    editUserError = error,
                    isEditing = false
                )
            }
        } else {
            val user = getUserFromInput(input)
            viewModelScope.launch {
                _editUser(
                    user,
                    input.avatar,
                    input.avatarAction
                ).onSuccess { editedUser ->
                    _state.update {
                        it.copy(
                            editedUser = editedUser,
                            isEditing = false,
                            editUserError = null
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            editUserError = error,
                            isEditing = false
                        )
                    }
                }
            }
        }
    }

    private fun getUserFromInput(input: EditUserInputState) = User(
        nick = input.nick,
        name = input.name.takeIf { it.isNotEmpty() },
        avatar = input.avatar,
        age = input.age.takeIf { it.isNotEmpty() }?.toByte(),
        bio = input.bio.takeIf { it.isNotEmpty() },
        tel = input.tel.takeIf { it.isNotEmpty() }?.toInt(),
        email = input.email.takeIf { it.isNotEmpty() }
    )

    private fun validateUserInput(input: EditUserInputState): CompoundError<EditUserError>? {
        val compoundError = CompoundError<EditUserError>()
        if (input.nick.isEmpty()) {
            compoundError.add(EditUserError.NICK_EMPTY)
        }
        if (input.age.toInt() !in 0..127) {
            compoundError.add(EditUserError.AGE_MALFORMED)
        }
        val telRegex = Regex("^\\+\\d{11,15}$")
        if (!telRegex.matches(input.tel) && input.tel.isNotEmpty()) {
            compoundError.add(EditUserError.TEL_MALFORMED)
        }
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}\$")
        if (!emailRegex.matches(input.email) && input.email.isNotEmpty()) {
            compoundError.add(EditUserError.EMAIL_MALFORMED)
        }
        return if (compoundError.isNotNull()) compoundError else null
    }

    fun removeUser() {
        _state.update {
            it.copy(
                isRemoving = true,
                isRemovalConfirmed = false
            )
        }
        viewModelScope.launch {
            _removeUser(userId).onSuccess {
                _state.value = EditUserScreenState(
                    isRemovalConfirmed = true
                )
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isRemoving = false,
                        isRemovalConfirmed = false,
                        removeUserError = error
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: Long): EditUserViewModel
    }
}