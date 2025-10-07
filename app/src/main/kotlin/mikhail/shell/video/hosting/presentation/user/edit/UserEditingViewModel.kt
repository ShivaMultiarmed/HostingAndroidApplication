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
import mikhail.shell.video.hosting.domain.ImageSize.MEDIUM
import mikhail.shell.video.hosting.domain.errors.UserEditingError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.NickCheckPurpose
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.usecases.user.ConstructAvatarUrl
import mikhail.shell.video.hosting.domain.usecases.user.EditUser
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.domain.usecases.user.RemoveUser
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateBio
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateEmail
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateName
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateNick
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateTelephone
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import mikhail.shell.video.hosting.presentation.utils.FieldState

@HiltViewModel(assistedFactory = UserEditingViewModel.Factory::class)
class UserEditingViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    private val getUser: GetUser,
    private val validateNick: ValidateNick,
    private val validateName: ValidateName,
    private val validateBio: ValidateBio,
    private val validateImage: ValidateImage,
    private val validateEmail: ValidateEmail,
    private val validateTelephone: ValidateTelephone,
    private val constructAvatarUrl: ConstructAvatarUrl,
    private val editUser: EditUser,
    private val removeUser: RemoveUser
) : ViewModel() {
    private val _state = MutableStateFlow<UserEditingScreenState>(UserEditingScreenState.Starting)
    val state = _state
        .onStart {
            start()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = _state.value
        )

    fun onEvent(event: UserEditingUiEvent) {
        when (event) {
            is UserEditingUiEvent.NickChanged -> onNickChanged(event.nick)
            UserEditingUiEvent.NickFocused -> onNickFocused()
            UserEditingUiEvent.NickBlurred -> onNickBlurred()
            is UserEditingUiEvent.NameChanged -> onNameChanged(event.name)
            UserEditingUiEvent.NameFocused -> onNameFocused()
            UserEditingUiEvent.NameBlurred -> onNameBlurred()
            is UserEditingUiEvent.AvatarChanged -> onAvatarChanged(event.avatar, event.action)
            is UserEditingUiEvent.BioChanged -> onBioChanged(event.bio)
            UserEditingUiEvent.BioFocused -> onBioFocused()
            UserEditingUiEvent.BioBlurred -> onBioBlurred()
            is UserEditingUiEvent.EmailChanged -> onEmailChanged(event.email)
            UserEditingUiEvent.EmailFocused -> onEmailFocused()
            UserEditingUiEvent.EmailBlurred -> onEmailBlurred()
            is UserEditingUiEvent.TelChanged -> onTelChanged(event.tel)
            UserEditingUiEvent.TelFocused -> onTelFocused()
            UserEditingUiEvent.TelBlurred -> onTelBlurred()
            UserEditingUiEvent.Restart -> start()
            UserEditingUiEvent.Remove -> remove()
            UserEditingUiEvent.Submit -> edit()
            else -> Unit
        }
    }

    private fun onNickChanged(nick: String) {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    nick = currentState.editedUser.nick.copy(
                        value = nick
                    )
                )
            ) ?: it
        }
    }

    private fun onNickFocused() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    nick = currentState.editedUser.nick.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onNickBlurred() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? UserEditingScreenState.Editing
                currentState?.copy(
                    editedUser = currentState.editedUser.copy(
                        nick = currentState.editedUser.nick.copy(
                            error = currentState.editedUser.nick.value.let {
                                val validationResult = validateNick(NickCheckPurpose.EDIT,it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        )
                    )
                ) ?: it
            }
        }
    }

    private fun onNameChanged(name: String) {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    name = currentState.editedUser.name.copy(
                        value = name
                    )
                )
            ) ?: it
        }
    }

    private fun onNameFocused() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    name = currentState.editedUser.name.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onNameBlurred() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    name = currentState.editedUser.name.copy(
                        error = currentState.editedUser.name.value.let {
                            val validationResult = validateName(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onBioChanged(bio: String) {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    bio = currentState.editedUser.name.copy(
                        value = bio
                    )
                )
            ) ?: it
        }
    }

    private fun onBioFocused() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    bio = currentState.editedUser.bio.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onBioBlurred() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    bio = currentState.editedUser.bio.copy(
                        error = currentState.editedUser.bio.value.let {
                            val validationResult = validateBio(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onTelChanged(tel: String) {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    tel = currentState.editedUser.tel.copy(
                        value = tel
                    )
                )
            ) ?: it
        }
    }

    private fun onTelFocused() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    tel = currentState.editedUser.tel.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onTelBlurred() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    tel = currentState.editedUser.tel.copy(
                        error = currentState.editedUser.tel.value.takeIf { it.isNotEmpty() }?.let {
                            val validationResult = validateTelephone(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onEmailChanged(email: String) {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    email = currentState.editedUser.email.copy(
                        value = email
                    )
                )
            ) ?: it
        }
    }

    private fun onEmailFocused() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    email = currentState.editedUser.email.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onEmailBlurred() {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    email = currentState.editedUser.email.copy(
                        error = currentState.editedUser.email.value.takeIf { it.isNotEmpty() }
                            ?.let {
                                val validationResult = validateEmail(it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                    )
                )
            ) ?: it
        }
    }

    private fun onAvatarChanged(avatar: String?, action: EditAction) {
        _state.update {
            val currentState = it as? UserEditingScreenState.Editing
            currentState?.copy(
                editedUser = currentState.editedUser.copy(
                    avatar = currentState.editedUser.avatar.copy(
                        value = avatar,
                        error = avatar?.let {
                            val validationResult = validateImage(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    avatarAction = action
                )
            ) ?: it
        }
    }

    private fun start() {
        viewModelScope.launch {
            getUser(userId).onSuccess { initialUser ->
                _state.update {
                    UserEditingScreenState.Editing(
                        initialUser = initialUser.toEditUi(avatar = constructAvatarUrl(userId, MEDIUM)),
                        editedUser = UserEditingInputState(
                            nick = FieldState(initialUser.nick),
                            name = FieldState(initialUser.name ?: ""),
                            bio = FieldState(initialUser.bio ?: ""),
                            tel = FieldState(initialUser.tel ?: ""),
                            email = FieldState(initialUser.email ?: "")
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
        viewModelScope.launch {
            _state.update {
                val currentState = it as? UserEditingScreenState.Editing
                currentState?.copy(
                    editedUser = currentState.editedUser.copy(
                        nick = currentState.editedUser.nick.copy(
                            error = currentState.editedUser.nick.value.let {
                                val validationResult = validateNick(NickCheckPurpose.EDIT,it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        ),
                        name = currentState.editedUser.name.copy(
                            error = currentState.editedUser.name.value.let {
                                val validationResult = validateName(it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        ),
                        bio = currentState.editedUser.bio.copy(
                            error = currentState.editedUser.bio.value.let {
                                val validationResult = validateBio(it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        ),
                        email = currentState.editedUser.email.copy(
                            error = currentState.editedUser.email.value.takeIf { it.isNotEmpty() }
                                ?.let {
                                    val validationResult = validateEmail(it)
                                    if (validationResult is Result.Failure) validationResult.error else null
                                }
                        ),
                        tel = currentState.editedUser.tel.copy(
                            error = currentState.editedUser.tel.value.takeIf { it.isNotEmpty() }?.let {
                                val validationResult = validateTelephone(it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        ),
                        avatar = currentState.editedUser.avatar.copy(
                            error = currentState.editedUser.avatar.value?.let {
                                val validationResult = validateImage(it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        )
                    )
                )?: it
            }
            val currentState = (_state.value as? UserEditingScreenState.Editing) ?: return@launch
            val input = currentState.editedUser
            if (
                input.nick.error != null && input.nick.error !is NetworkError
                || input.name.error != null
                || input.bio.error != null
                || input.avatar.error != null
                || input.email.error != null
                || input.tel.error != null
                ) {
                return@launch
            }
            val user = User(
                userId = userId,
                nick = input.nick.value,
                name = input.name.value.takeIf { it.isNotEmpty() },
                bio = input.bio.value.takeIf { it.isNotEmpty() },
                tel = input.tel.value.takeIf { it.isNotEmpty() }?.removePrefix("+"),
                email = input.email.value.takeIf { it.isNotEmpty() }
            )
            editUser(
                user = user,
                avatar = input.avatar.value,
                avatarAction = input.avatarAction
            ).onSuccess {
                _state.update {
                    UserEditingScreenState.Success
                }
            }.onFailure { error ->
                _state.update {
                    if (error !is UserEditingError) {
                        currentState.copy(
                            error = error,
                            isLoading = false
                        )
                    } else {
                        currentState.copy(
                            editedUser = currentState.editedUser.copy(
                                nick = currentState.editedUser.nick.copy(
                                    error = error.nickError
                                ),
                                name = currentState.editedUser.name.copy(
                                    error = error.nameError
                                ),
                                bio = currentState.editedUser.bio.copy(
                                    error = error.bioError
                                ),
                                email = currentState.editedUser.email.copy(
                                    error = error.emailError
                                ),
                                tel = currentState.editedUser.tel.copy(
                                    error = error.telError
                                ),
                                avatar = currentState.editedUser.avatar.copy(
                                    error = error.avatarError
                                )
                            ),
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun remove() {
        val currentState = (_state.value as? UserEditingScreenState.Editing) ?: return
        _state.update {
            currentState.copy(isRemoving = true)
        }
        viewModelScope.launch {
            removeUser().onSuccess {
                _state.update {
                    UserEditingScreenState.Removed
                }
            }.onFailure { error ->
                _state.update {
                    currentState.copy(
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
    data class NickChanged(val nick: String) : UserEditingUiEvent()
    data object NickFocused : UserEditingUiEvent()
    data object NickBlurred : UserEditingUiEvent()
    data class NameChanged(val name: String) : UserEditingUiEvent()
    data object NameFocused : UserEditingUiEvent()
    data object NameBlurred : UserEditingUiEvent()
    data class AvatarChanged(val avatar: String?, val action: EditAction) : UserEditingUiEvent()
    data class BioChanged(val bio: String) : UserEditingUiEvent()
    data object BioFocused : UserEditingUiEvent()
    data object BioBlurred : UserEditingUiEvent()
    data class TelChanged(val tel: String) : UserEditingUiEvent()
    data object TelFocused : UserEditingUiEvent()
    data object TelBlurred : UserEditingUiEvent()
    data class EmailChanged(val email: String) : UserEditingUiEvent()
    data object EmailFocused : UserEditingUiEvent()
    data object EmailBlurred : UserEditingUiEvent()
    data object Submit : UserEditingUiEvent()
    data object Cancel : UserEditingUiEvent()
    data object Restart : UserEditingUiEvent()
    data object Remove : UserEditingUiEvent()
}