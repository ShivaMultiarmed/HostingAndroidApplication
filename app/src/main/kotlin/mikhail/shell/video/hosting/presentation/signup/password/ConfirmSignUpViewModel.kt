package mikhail.shell.video.hosting.presentation.signup.password

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
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.ConfirmSignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateNick
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePasswordDuplicate

@HiltViewModel(assistedFactory = ConfirmSignUpViewModel.Factory::class)
class ConfirmSignUpViewModel @AssistedInject constructor(
    @Assisted("token") private val token: String,
    private val validatePassword: ValidatePassword,
    private val validatePasswordDuplicate: ValidatePasswordDuplicate,
    private val validateNick: ValidateNick,
    private val confirm: ConfirmSignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow<ConfirmSignUpScreenState>(ConfirmSignUpScreenState.Entering())
    val state = _state.asStateFlow()

    fun onEvent(event: ConfirmSignUpUiEvent) {
        when (event) {
            is ConfirmSignUpUiEvent.NickChanged -> onNickChanged(event.nick)
            ConfirmSignUpUiEvent.NickTypingStarted -> onNickTypingStarted()
            ConfirmSignUpUiEvent.NickTypingEnded -> onNickTypingEnded()
            is ConfirmSignUpUiEvent.PasswordChanged -> onPasswordChanged(event.password)
            ConfirmSignUpUiEvent.PasswordTypingStarted -> onPasswordTypingStarted()
            ConfirmSignUpUiEvent.PasswordTypingEnded -> onPasswordTypingStarted()
            is ConfirmSignUpUiEvent.PasswordDuplicateChanged -> onPasswordDuplicateChanged(event.passwordDuplicate)
            ConfirmSignUpUiEvent.PasswordDuplicateTypingStarted -> onPasswordDuplicateTypingStarted()
            ConfirmSignUpUiEvent.PasswordDuplicateTypingEnded -> onPasswordDuplicateTypingStarted()
            ConfirmSignUpUiEvent.Submit -> confirm()
        }
    }

    private fun onNickChanged(nick: String) {
        viewModelScope.launch {
            _state.update {
                it as ConfirmSignUpScreenState.Entering
                it.copy(
                    input = it.input.copy(
                        nick = nick
                    )
                )
            }
        }
    }

    private fun onNickTypingStarted() {
        viewModelScope.launch {
            _state.update {
                it as ConfirmSignUpScreenState.Entering
                it.copy(
                    input = it.input.copy(
                        nickError = null
                    )
                )
            }
        }
    }

    private fun onNickTypingEnded() {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState as ConfirmSignUpScreenState.Entering
                currentState.copy(
                    input = currentState.input.copy(
                        nickError = currentState.input.nick.let {
                            val validationResult = validateNick(it)
                            if (validationResult is Result.Failure) {
                                validationResult.error
                            } else {
                                validationResult as Result.Success
                                if (validationResult.data) {
                                    TextError.EXISTS
                                } else {
                                    null
                                }
                            }
                        }
                    )
                )
            }
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it as ConfirmSignUpScreenState.Entering
            it.copy(
                input = it.input.copy(
                    password = password
                )
            )
        }
    }

    private fun onPasswordTypingStarted() {
        _state.update {
            it as ConfirmSignUpScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordError = null
                )
            )
        }
    }

    private fun onPasswordTypingEnded(password: String) {
        _state.update {
            it as ConfirmSignUpScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordError = password.let {
                        val validationResult = validatePassword(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onPasswordDuplicateChanged(passwordDuplicate: String) {
        _state.update {
            it as ConfirmSignUpScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordDuplicate = passwordDuplicate
                )
            )
        }
    }

    private fun onPasswordDuplicateTypingStarted() {
        _state.update {
            it as ConfirmSignUpScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordDuplicateError = null
                )
            )
        }
    }

    private fun onPasswordDuplicateTypingEnded() {
        _state.update {
            it as ConfirmSignUpScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordDuplicateError = it.input.passwordDuplicate.let { passwordDuplicate ->
                        val validationResult = validatePasswordDuplicate(
                            password = it.input.password,
                            passwordDuplicate = passwordDuplicate
                        )
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun confirm() {
        val currentState = _state.value as ConfirmSignUpScreenState.Entering
        val user = User(
            nick = currentState.input.nick
        )
        viewModelScope.launch {
            confirm(
                token = token,
                password = currentState.input.password,
                user = user
            ).onSuccess { authModel ->
                _state.update {
                    ConfirmSignUpScreenState.Success(authModel)
                }
            }.onFailure { error ->
                _state.update {
                    if (error != TextError.NOT_VALID) {
                        it as ConfirmSignUpScreenState.Entering
                        it.copy(
                            error = error
                        )
                    } else {
                        ConfirmSignUpScreenState.Expired
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("token") token: String): ConfirmSignUpViewModel
    }
}

sealed class ConfirmSignUpUiEvent {
    data class NickChanged(val nick: String): ConfirmSignUpUiEvent()
    data object NickTypingStarted: ConfirmSignUpUiEvent()
    data object NickTypingEnded: ConfirmSignUpUiEvent()
    data class PasswordChanged(val password: String): ConfirmSignUpUiEvent()
    data object PasswordTypingStarted: ConfirmSignUpUiEvent()
    data object PasswordTypingEnded: ConfirmSignUpUiEvent()
    data class PasswordDuplicateChanged(val passwordDuplicate: String): ConfirmSignUpUiEvent()
    data object PasswordDuplicateTypingStarted: ConfirmSignUpUiEvent()
    data object PasswordDuplicateTypingEnded: ConfirmSignUpUiEvent()
    data object Submit: ConfirmSignUpUiEvent()
}