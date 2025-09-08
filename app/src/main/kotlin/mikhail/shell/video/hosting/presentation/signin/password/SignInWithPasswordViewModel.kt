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
import mikhail.shell.video.hosting.domain.usecases.authentication.SignInWithPassword
import mikhail.shell.video.hosting.domain.usecases.channels.SubscribeToNotifications
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateUserName
import mikhail.shell.video.hosting.domain.validation.CheckUserName
import javax.inject.Inject

@HiltViewModel
class SignInWithPasswordViewModel @Inject constructor(
    private val validateUserName: ValidateUserName,
    private val checkUserName: CheckUserName,
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
            SignInUiEvent.UserNameTypingStarted -> onUserNameTypingStarted()
            SignInUiEvent.UserNameTypingEnded -> onUserNameTypingEnded()
            else -> null
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it as SignInScreenState.Entering
            it.copy(
                input = it.input.copy(
                    password = it.input.password.copy(
                        value = password,
                        error = it.input.password.value.let {
                            val validationResult = validatePassword(password)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            )
        }
    }

    private fun onUserNameChanged(userName: String) {
        _state.update {
            it as SignInScreenState.Entering
            it.copy(
                input = it.input.copy(
                    userName = it.input.userName.copy(
                        value = userName,
                        error = it.input.userName.value.let {
                            val validationResult = validateUserName(userName)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    )
                )
            )
        }
    }

    private fun onUserNameTypingStarted() {
        _state.update {
            it as SignInScreenState.Entering
            it.copy(
                input = it.input.copy(
                    userName = it.input.userName.copy(
                        isTyping = true
                    )
                )
            )
        }
    }

    private fun onUserNameTypingEnded() {
        viewModelScope.launch {
            _state.update {
                it as SignInScreenState.Entering
                it.copy(
                    input = it.input.copy(
                        userName = it.input.userName.copy(
                            isTyping = false,
                            error = if (it.input.userName.error == null || it.input.userName.error == TextError.NOT_EXISTS) {
                                val checkResult = checkUserName(it.input.userName.value)
                                when (checkResult) {
                                    is Result.Failure -> checkResult.error
                                    is Result.Success if (!checkResult.data) -> TextError.NOT_EXISTS
                                    else -> null
                                }
                            } else {
                                it.input.userName.error
                            }
                        )
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
                email = currentInput.userName.value,
                password = currentInput.password.value
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
                        error = if (error !in listOf(NetworkError.NOT_FOUND, NetworkError.BAD_REQUEST)) error else it.error,
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
    data object UserNameTypingStarted : SignInUiEvent()
    data object UserNameTypingEnded : SignInUiEvent()
    data class PasswordChanged(val password: String) : SignInUiEvent()
    data object Submit : SignInUiEvent()
    data object SignUp : SignInUiEvent()
}