package mikhail.shell.video.hosting.presentation.signin.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.authentication.SignInWithPassword
import mikhail.shell.video.hosting.domain.usecases.channels.SubscribeToNotifications
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
    private val _state = MutableStateFlow<SignInScreenState>(SignInScreenState.Entering())
    val state = _state.asStateFlow()

    fun onEvent(event: SignInUiEvent) {
        when (event) {
            SignInUiEvent.Submit -> signIn()
            is SignInUiEvent.PasswordChanged -> onPasswordChanged(event.password)
            is SignInUiEvent.UserNameChanged -> onUserNameChanged(event.userName)
            else -> null
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it as SignInScreenState.Entering
            it.copy(
                input = it.input.copy(
                    password = password,
                    passwordError = password.let {
                        val validationResult = validatePassword(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onUserNameChanged(userName: String) {
        viewModelScope.launch {
            _state.update {
                it as SignInScreenState.Entering
                it.copy(
                    input = it.input.copy(
                        userName = userName,
                        userNameError = userName.let {
                            val validationResult = validateUserName(it)
                            if (validationResult is Result.Failure) {
                                validationResult.error
                            } else {
                                validationResult as Result.Success
                                if (!validationResult.data) TextError.NOT_EXISTS else null
                            }
                        }
                    )
                )
            }
        }
    }

    private fun signIn() {
        _state.update {
            it as SignInScreenState.Entering
            it.copy(isLoading = true)
        }
        val currentInput = (_state.value as SignInScreenState.Entering).input
        viewModelScope.launch {
            signInWithPassword(
                email = currentInput.userName,
                password = currentInput.password
            ).onSuccess { authModel ->
                subscribeToNotifications()
                _state.update {
                    SignInScreenState.Success(authModel)
                }
            }.onFailure { error ->
                _state.update {
                    it as SignInScreenState.Entering
                    it.copy(
                        isLoading = false,
                        error = if (error != TextError.NOT_CORRECT) error else it.error,
                        input = it.input.copy(
                            passwordError = TextError.NOT_CORRECT
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
    data class UserNameChanged(val userName: String): SignInUiEvent()
    data class PasswordChanged(val password: String): SignInUiEvent()
    data object Submit: SignInUiEvent()
    data object SignUp: SignInUiEvent()
}