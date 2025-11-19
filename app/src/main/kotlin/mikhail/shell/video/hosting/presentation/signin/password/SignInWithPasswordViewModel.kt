package mikhail.shell.video.hosting.presentation.signin.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.authentication.SignInWithPassword
import mikhail.shell.video.hosting.domain.usecases.channels.SubscribeToNotifications
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateUserName
import javax.inject.Inject

@HiltViewModel
class SignInWithPasswordViewModel @Inject constructor(
    private val validateUserName: ValidateUserName,
    private val validatePassword: ValidatePassword,
    private val signInWithPassword: SignInWithPassword,
    private val subscribeToNotifications: SubscribeToNotifications
) : ViewModel() {
    private val _state = MutableStateFlow(SignInScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: SignInUiEvent) {
        when (event) {
            SignInUiEvent.Submit -> signIn()
            is SignInUiEvent.PasswordChanged -> onPasswordChanged(event.password)
            is SignInUiEvent.UserNameChanged -> onUserNameChanged(event.userName)
            SignInUiEvent.UserNameFocused -> clearUserNameError()
            SignInUiEvent.UserNameBlurred -> validateUserName()
            SignInUiEvent.PasswordFocused -> clearPasswordError()
            SignInUiEvent.PasswordBlurred -> validatePassword()
            else -> null
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it.copy(
                input = it.input.copy(
                    password = it.input.password.copy(
                        value = password
                    )
                )
            )
        }
    }

    private fun clearPasswordError() {
        _state.update {
            it.copy(
                input = it.input.copy(
                    password = it.input.password.copy(
                        error = null
                    )
                )
            )
        }
    }

    private fun validatePassword() {
        _state.update {
            it.copy(
                input = it.input.copy(
                    password = it.input.password.copy(
                        error = it.input.password.value.let {
                            val validationResult = validatePassword(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            )
        }
    }

    private fun onUserNameChanged(userName: String) {
        _state.update {
            it.copy(
                input = it.input.copy(
                    userName = it.input.userName.copy(value = userName)
                )
            )
        }
    }

    private fun clearUserNameError() {
        _state.update {
            it.copy(
                input = it.input.copy(
                    userName = it.input.userName.copy(error = null)
                )
            )
        }
    }

    private fun validateUserName() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    input = it.input.copy(
                        userName = it.input.userName.copy(
                            error = it.input.userName.value.let {
                                validateUserName(UserNameCheckPurpose.SIGN_IN,it).errorOrNull()
                            }
                        )
                    )
                )
            }
        }
    }

    private fun signIn() {
        validateUserName()
        validatePassword()
        val currentInput = _state.value.input
        if (currentInput.userName.error != null || currentInput.password.error != null) {
            return
        }
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            signInWithPassword(
                email = currentInput.userName.value,
                password = currentInput.password.value
            ).onSuccess { authModel ->
                subscribeToNotifications()
                _state.update {
                    it.copy(
                        error = null,
                        isLoading = false,
                        authModel = authModel
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = if (error !in listOf(
                                NetworkError.NOT_FOUND,
                                NetworkError.BAD_REQUEST
                            )
                        ) error else it.error,
                        input = it.input.copy(
                            userName = it.input.userName.copy(
                                error = if (error == NetworkError.NOT_FOUND) TextError.NOT_EXISTS else it.input.password.error
                            ),
                            password = it.input.password.copy(
                                error = if (error == NetworkError.BAD_REQUEST) TextError.NOT_CORRECT else it.input.password.error
                            )
                        )
                    )
                }
            }
        }

    }

    private fun subscribeToNotifications() {
        viewModelScope.launch {
            subscribeToNotifications.invoke()
        }
    }
}

sealed class SignInUiEvent {
    data class UserNameChanged(val userName: String) : SignInUiEvent()
    data object UserNameFocused : SignInUiEvent()
    data object UserNameBlurred : SignInUiEvent()
    data class PasswordChanged(val password: String) : SignInUiEvent()
    data object PasswordFocused : SignInUiEvent()
    data object PasswordBlurred : SignInUiEvent()
    data object Submit : SignInUiEvent()
    data object SignUp : SignInUiEvent()
}