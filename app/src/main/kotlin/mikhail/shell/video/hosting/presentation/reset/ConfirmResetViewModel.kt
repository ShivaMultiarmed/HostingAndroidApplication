package mikhail.shell.video.hosting.presentation.reset

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
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.ConfirmResetPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePasswordDuplicate

@HiltViewModel(assistedFactory = ConfirmResetViewModel.Factory::class)
class ConfirmResetViewModel @AssistedInject constructor(
    @Assisted("token") private val token: String,
    private val validatePassword: ValidatePassword,
    private val validatePasswordDuplicate: ValidatePasswordDuplicate,
    private val confirm: ConfirmResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow<ConfirmResetScreenState>(ConfirmResetScreenState.Entering())
    val state = _state.asStateFlow()

    fun onEvent(event: ConfirmResetUiEvent) {
        when (event) {
            is ConfirmResetUiEvent.PasswordChanged -> onPasswordChanged(event.password)
            ConfirmResetUiEvent.PasswordTypingStarted -> TODO()
            ConfirmResetUiEvent.PasswordTypingEnded -> TODO()
            is ConfirmResetUiEvent.PasswordDuplicatedChanged -> onPasswordDuplicateChanged(event.passwordDuplicate)
            ConfirmResetUiEvent.PasswordDuplicatedTypingStarted -> TODO()
            ConfirmResetUiEvent.PasswordDuplicatedTypingEnded -> TODO()
            ConfirmResetUiEvent.Submit -> confirm()
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it as ConfirmResetScreenState.Entering
            it.copy(
                input = it.input.copy(
                    password = password
                )
            )
        }
    }
    private fun onPasswordTypingStarted() {
        _state.update {
            it as ConfirmResetScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordError = null
                )
            )
        }
    }
    private fun onPasswordTypingEnded() {
        _state.update {
            it as ConfirmResetScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordError = it.input.password.let {
                        val validationResult = validatePassword(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onPasswordDuplicateChanged(passwordDuplicate: String) {
        _state.update {
            it as ConfirmResetScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordDuplicate = passwordDuplicate
                )
            )
        }
    }
    private fun onPasswordDuplicateTypingStarted() {
        _state.update {
            it as ConfirmResetScreenState.Entering
            it.copy(
                input = it.input.copy(
                    passwordDuplicateError = null
                )
            )
        }
    }
    private fun onPasswordDuplicateTypingEnded() {
        _state.update {
            it as ConfirmResetScreenState.Entering
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
        _state.update {
            it as ConfirmResetScreenState.Entering
            it.copy(isLoading = true)
        }
        val input = (_state.value as ConfirmResetScreenState.Entering).input
        viewModelScope.launch {
            confirm(
                token = token,
                password = input.password
            ).onSuccess { authModel ->
                _state.update {
                    ConfirmResetScreenState.Success(authModel)
                }
            }.onFailure { error ->
                _state.update {
                    it as ConfirmResetScreenState.Entering
                    it.copy(
                        error = error,
                        isLoading = false
                    )
                }
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("token") token: String): ConfirmResetViewModel
    }
}

sealed class ConfirmResetUiEvent {
    data class PasswordChanged(val password: String): ConfirmResetUiEvent()
    data object PasswordTypingStarted: ConfirmResetUiEvent()
    data object PasswordTypingEnded: ConfirmResetUiEvent()
    data class PasswordDuplicatedChanged(val passwordDuplicate: String): ConfirmResetUiEvent()
    data object PasswordDuplicatedTypingStarted: ConfirmResetUiEvent()
    data object PasswordDuplicatedTypingEnded: ConfirmResetUiEvent()
    data object Submit: ConfirmResetUiEvent()
}