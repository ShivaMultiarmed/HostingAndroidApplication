package mikhail.shell.video.hosting.presentation.signin.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.authentication.SignInError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.usecases.authentication.SignIn
import mikhail.shell.video.hosting.domain.usecases.user.SubscribeToNotifications
import mikhail.shell.video.hosting.domain.usecases.user.SaveUserDetails
import mikhail.shell.video.hosting.domain.models.UserNameCheckPurpose
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidatePassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateUserName
import javax.inject.Inject
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenState as ScreenState

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val saveUserDetails: SaveUserDetails,
    private val validateUserName: ValidateUserName,
    private val validatePassword: ValidatePassword,
    private val signIn: SignIn,
    private val subscribeToNotifications: SubscribeToNotifications
) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            ScreenAction.Submit -> signIn()
            is ScreenAction.PasswordChanged -> onPasswordChanged(action.password)
            is ScreenAction.UserNameChanged -> onUserNameChanged(action.userName)
            ScreenAction.UserNameFocused -> clearUserNameError()
            ScreenAction.UserNameBlurred -> validateUserName()
            ScreenAction.PasswordFocused -> clearPasswordError()
            ScreenAction.PasswordBlurred -> validatePassword()
            ScreenAction.ResetPassword -> viewModelScope.launch {
                _events.emit(ScreenEvent.ResetRequested)
            }
            ScreenAction.SignUp -> viewModelScope.launch {
                _events.emit(ScreenEvent.SignUpRequested)
            }
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it.copy(
                input = it.input.copy(
                    password = it.input.password.copy(value = password)
                )
            )
        }
    }

    private fun clearPasswordError() {
        _state.update {
            it.copy(
                input = it.input.copy(
                    password = it.input.password.copy(error = null)
                )
            )
        }
    }

    private fun validatePassword() {
        _state.update {
            it.copy(
                input = it.input.copy(
                    password = it.input.password.copy(
                        error = validatePassword(it.input.password.value).errorOrNull()
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
        if (_state.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    input = it.input.copy(
                        userName = it.input.userName.copy(
                            error = validateUserName(UserNameCheckPurpose.SIGN_IN, it.input.userName.value).errorOrNull()
                        )
                    )
                )
            }
        }
    }

    private fun signIn() {
        if (_state.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    input = it.input.copy(
                        userName = it.input.userName.copy(
                            error = validateUserName(UserNameCheckPurpose.SIGN_IN, it.input.userName.value).errorOrNull()
                        ),
                        password = it.input.password.copy(
                            error = validatePassword(it.input.password.value).errorOrNull()
                        )
                    )
                )
            }
            if (
                _state.value.input.userName.error != null && _state.value.input.userName.error !is NetworkError
                || _state.value.input.password.error != null
            ) {
                return@launch
            }
            _state.update {
                it.copy(isLoading = true)
            }
            signIn(
                email = _state.value.input.userName.value,
                password = _state.value.input.password.value
            ).onSuccess { authModel ->
                viewModelScope.launch {
                    saveUserDetails(
                        UserDetails(
                            userId = authModel.userId,
                            token = authModel.token
                        )
                    )
                    subscribeToNotifications()
                    _events.emit(ScreenEvent.Success(authModel))
                    _state.update {
                        it.copy(isLoading = false)
                    }
                }
            }.onFailure { error ->
                if (error is SignInError) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            input = it.input.copy(
                                userName = it.input.userName.copy(error = error.userNameError),
                                password = it.input.password.copy(error = error.passwordError)
                            )
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(ScreenEvent.Failure(error))
                    }
                    _state.update {
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }
}