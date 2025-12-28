package mikhail.shell.video.hosting.presentation.signup.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.user.UserCreationError
import mikhail.shell.video.hosting.domain.models.NickCheckPurpose
import mikhail.shell.video.hosting.domain.models.UserCreationModel
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.ConfirmSignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.user.SaveUserDetails
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateNick
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePasswordDuplicate
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.signup.password.SignUpConfirmationScreenState as ScreenState

@HiltViewModel(assistedFactory = SignUpConfirmationViewModel.Factory::class)
class SignUpConfirmationViewModel @AssistedInject constructor(
    @Assisted("token") private val token: String,
    private val saveUserDetails: SaveUserDetails,
    private val validatePassword: ValidatePassword,
    private val validatePasswordDuplicate: ValidatePasswordDuplicate,
    private val validateNick: ValidateNick,
    private val confirm: ConfirmSignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            is ScreenAction.NickChanged -> onNickChanged(action.nick)
            ScreenAction.NickFocused -> onNickFocused()
            ScreenAction.NickBlurred -> onNickBlurred()
            is ScreenAction.PasswordChanged -> onPasswordChanged(action.password)
            ScreenAction.PasswordFocused -> onPasswordFocused()
            ScreenAction.PasswordBlurred -> onPasswordBlurred()
            is ScreenAction.PasswordDuplicateChanged -> onPasswordDuplicateChanged(action.passwordDuplicate)
            ScreenAction.PasswordDuplicateFocused -> onPasswordDuplicateFocused()
            ScreenAction.PasswordDuplicateBlurred -> onPasswordDuplicateBlurred()
            ScreenAction.Submit -> confirm()
        }
    }

    private fun onNickChanged(nick: String) {
        _state.update {
            it.copy(
                user = it.user.copy(
                    nick = it.user.nick.copy(value = nick)
                )
            )
        }
    }

    private fun onNickFocused() {
        _state.update {
            it.copy(
                user = it.user.copy(
                    nick = it.user.nick.copy(error = null)
                )
            )
        }
    }

    private fun onNickBlurred() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    user = it.user.copy(
                        nick = it.user.nick.copy(
                            error = validateNick(
                                NickCheckPurpose.SIGN_UP,
                                it.user.nick.value
                            ).errorOrNull()
                        )
                    )
                )
            }
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it.copy(
                user = it.user.copy(
                    password = it.user.password.copy(value = password)
                )
            )
        }
    }

    private fun onPasswordFocused() {
        _state.update {
            it.copy(
                user = it.user.copy(
                    password = it.user.password.copy(error = null)
                )
            )
        }
    }

    private fun onPasswordBlurred() {
        _state.update {
            it.copy(
                user = it.user.copy(
                    password = it.user.password.copy(
                        error = validatePassword(it.user.password.value).errorOrNull()
                    )
                )
            )
        }
    }

    private fun onPasswordDuplicateChanged(passwordDuplicate: String) {
        _state.update {
            it.copy(
                user = it.user.copy(
                    passwordDuplicate = it.user.passwordDuplicate.copy(value = passwordDuplicate)
                )
            )
        }
    }

    private fun onPasswordDuplicateFocused() {
        _state.update {
            it.copy(
                user = it.user.copy(
                    passwordDuplicate = it.user.passwordDuplicate.copy(error = null)
                )
            )
        }
    }

    private fun onPasswordDuplicateBlurred() {
        _state.update {
            it.copy(
                user = it.user.copy(
                    passwordDuplicate = it.user.passwordDuplicate.copy(
                        error = validatePasswordDuplicate(
                            password = it.user.password.value,
                            passwordDuplicate = it.user.passwordDuplicate.value
                        ).errorOrNull()
                    )
                )
            )
        }
    }

    private fun confirm() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    user = it.user.copy(
                        nick = it.user.nick.copy(
                            error = validateNick(
                                NickCheckPurpose.SIGN_UP,
                                it.user.nick.value
                            ).errorOrNull()
                        ),
                        password = it.user.password.copy(
                            error = validatePassword(it.user.password.value).errorOrNull()
                        ),
                        passwordDuplicate = it.user.passwordDuplicate.copy(
                            error = validatePasswordDuplicate(
                                password = it.user.password.value,
                                passwordDuplicate = it.user.passwordDuplicate.value
                            ).errorOrNull()
                        )
                    )
                )
            }
            if (
                _state.value.user.nick.error != null
                || _state.value.user.password.error != null
                || _state.value.user.passwordDuplicate.error != null
            ) {
                return@launch
            }
            _state.update {
                it.copy(isLoading = true)
            }
            confirm(
                UserCreationModel(
                    nick = _state.value.user.nick.value,
                    token = token,
                    password = _state.value.user.password.value
                )
            ).onSuccess { authModel ->
                viewModelScope.launch {
                    saveUserDetails(
                        UserDetails(
                            userId = authModel.userId,
                            token = authModel.token
                        )
                    )
                    _events.emit(ScreenEvent.Success(authModel))
                    _state.update {
                        it.copy(isLoading = false)
                    }
                }
            }.onFailure { error ->
                if (error is UserCreationError) {
                    _state.update {
                        it.copy(
                            user = it.user.copy(
                                password = it.user.password.copy(
                                    error = error.passwordError,
                                ),
                                nick = it.user.nick.copy(
                                    error = error.nickError
                                )
                            )
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(ScreenEvent.Failure(error))
                    }
                }
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("token") token: String): SignUpConfirmationViewModel
    }
}