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
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.ConfirmSignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateNick
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePasswordDuplicate

@HiltViewModel(assistedFactory = SignUpConfirmationViewModel.Factory::class)
class SignUpConfirmationViewModel @AssistedInject constructor(
    @Assisted("token") private val token: String,
    private val validatePassword: ValidatePassword,
    private val validatePasswordDuplicate: ValidatePasswordDuplicate,
    private val validateNick: ValidateNick,
    private val confirm: ConfirmSignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpConfirmationState())
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<SignUpConfirmationEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: SignUpConfirmationAction) {
        when (action) {
            is SignUpConfirmationAction.NickChanged -> onNickChanged(action.nick)
            SignUpConfirmationAction.NickFocused -> onNickFocused()
            SignUpConfirmationAction.NickBlurred -> onNickBlurred()
            is SignUpConfirmationAction.PasswordChanged -> onPasswordChanged(action.password)
            SignUpConfirmationAction.PasswordFocused -> onPasswordFocused()
            SignUpConfirmationAction.PasswordBlurred -> onPasswordBlurred()
            is SignUpConfirmationAction.PasswordDuplicateChanged -> onPasswordDuplicateChanged(
                action.passwordDuplicate
            )

            SignUpConfirmationAction.PasswordDuplicateFocused -> onPasswordDuplicateFocused()
            SignUpConfirmationAction.PasswordDuplicateBlurred -> onPasswordDuplicateBlurred()
            SignUpConfirmationAction.Submit -> confirm()
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
                            error = it.user.nick.value.let {
                                validateNick(NickCheckPurpose.SIGN_UP, it).errorOrNull()
                            }
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
                        error = it.user.password.value.let {
                            validatePassword(it).errorOrNull()
                        }
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
                        error = it.user.passwordDuplicate.value.let { passDuplicate ->
                            validatePasswordDuplicate(
                                password = it.user.password.value,
                                passwordDuplicate = passDuplicate
                            ).errorOrNull()
                        }
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
                            error = it.user.nick.value.let {
                                validateNick(NickCheckPurpose.SIGN_UP, it).errorOrNull()
                            }
                        ),
                        password = it.user.password.copy(
                            error = it.user.password.value.let {
                                validatePassword(it).errorOrNull()
                            }
                        ),
                        passwordDuplicate = it.user.passwordDuplicate.copy(
                            error = it.user.passwordDuplicate.value.let { passDuplicate ->
                                validatePasswordDuplicate(
                                    password = it.user.password.value,
                                    passwordDuplicate = passDuplicate
                                ).errorOrNull()
                            }
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
                _state.update {
                    it.copy(isLoading = false)
                }
                viewModelScope.launch {
                    _events.emit(SignUpConfirmationEvent.Success(authModel))
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false)
                }
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
                    viewModelScope.launch   {
                        _events.emit(SignUpConfirmationEvent.Failure(error))
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("token") token: String): SignUpConfirmationViewModel
    }
}

sealed class SignUpConfirmationAction {
    data class NickChanged(val nick: String) : SignUpConfirmationAction()
    data object NickFocused : SignUpConfirmationAction()
    data object NickBlurred : SignUpConfirmationAction()
    data class PasswordChanged(val password: String) : SignUpConfirmationAction()
    data object PasswordFocused : SignUpConfirmationAction()
    data object PasswordBlurred : SignUpConfirmationAction()
    data class PasswordDuplicateChanged(val passwordDuplicate: String) : SignUpConfirmationAction()
    data object PasswordDuplicateFocused : SignUpConfirmationAction()
    data object PasswordDuplicateBlurred : SignUpConfirmationAction()
    data object Submit : SignUpConfirmationAction()
}