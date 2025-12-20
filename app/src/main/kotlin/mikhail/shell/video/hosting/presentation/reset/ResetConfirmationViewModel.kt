package mikhail.shell.video.hosting.presentation.reset

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
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.ConfirmResetPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePasswordDuplicate
import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.reset.ResetConfirmationScreenState as ScreenState

@HiltViewModel(assistedFactory = ResetConfirmationViewModel.Factory::class)
class ResetConfirmationViewModel @AssistedInject constructor(
    @Assisted("token") private val token: String,
    private val validatePassword: ValidatePassword,
    private val validatePasswordDuplicate: ValidatePasswordDuplicate,
    private val confirm: ConfirmResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            is ScreenAction.PasswordChanged -> onPasswordChanged(action.password)
            ScreenAction.PasswordFocused -> onPasswordFocused()
            ScreenAction.PasswordBlurred -> onPasswordBlurred()
            is ScreenAction.PasswordDuplicatedChanged -> onPasswordDuplicateChanged(action.passwordDuplicate)
            ScreenAction.PasswordDuplicatedFocused -> onPasswordDuplicateFocused()
            ScreenAction.PasswordDuplicatedBlurred -> onPasswordDuplicateBlurred()
            ScreenAction.Submit -> confirm()
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
        _state.update {
            it.copy(
                user = it.user.copy(
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
            _state.value.user.password.error != null
            || _state.value.user.passwordDuplicate.error != null
            ) {
            return
        }
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            confirm(
                token = token,
                password = _state.value.user.password.value
            ).onSuccess {
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Success(it))
                }
            }.onFailure { error ->
                if (error is TextError) {
                    _state.update {
                        it.copy(
                            user = it.user.copy(
                                password = it.user.password.copy(error = error)
                            )
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(ScreenEvent.Failure(error))
                    }
                }
            }
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("token") token: String): ResetConfirmationViewModel
    }
}