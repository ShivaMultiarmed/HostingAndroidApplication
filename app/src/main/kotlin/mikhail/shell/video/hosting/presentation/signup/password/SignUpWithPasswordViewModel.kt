package mikhail.shell.video.hosting.presentation.signup.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.errors.SignUpError.NICK_EMPTY
import mikhail.shell.video.hosting.domain.errors.SignUpError.PASSWORDS_NOT_MATCH
import mikhail.shell.video.hosting.domain.errors.SignUpError.PASSWORD_EMPTY
import mikhail.shell.video.hosting.domain.errors.SignUpError.USERNAME_EMPTY
import mikhail.shell.video.hosting.domain.errors.SignUpError.USERNAME_MALFORMED
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.usecases.authentication.SignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.channels.SubscribeToChannelNotifications
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import javax.inject.Inject

@HiltViewModel
class SignUpWithPasswordViewModel @Inject constructor(
    private val _signUpWithPassword: SignUpWithPassword,
    private val _subscribeToChannelNotifications: SubscribeToChannelNotifications
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpWithPasswordState())
    val state = _state.asStateFlow()

    private companion object {
        private val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    }

    private fun validateSignUpInput(inputState: SignUpInputState): CompoundError<SignUpError>? {
        val error = CompoundError<SignUpError>()
        if (inputState.userName.isEmpty()) {
            error.add(USERNAME_EMPTY)
        } else if (!emailRegex.matches(inputState.userName)) {
            error.add(USERNAME_MALFORMED)
        }
        if (inputState.password.isEmpty()) {
            error.add(PASSWORD_EMPTY)
        } else if (inputState.password != inputState.passwordDuplicate) {
            error.add(PASSWORDS_NOT_MATCH)
        }
        if (inputState.nick.isEmpty()) {
            error.add(NICK_EMPTY)
        }
        return error.takeIf { it.isNotNull() }
    }

    fun signUp(signUpInputState: SignUpInputState) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        val compoundError = validateSignUpInput(signUpInputState)
        if (compoundError == null) {
            val user = User(
                nick = signUpInputState.nick
            )
            viewModelScope.launch {
                _signUpWithPassword(
                    signUpInputState.userName,
                    signUpInputState.password,
                    user
                ).onSuccess {
                    _state.value = SignUpWithPasswordState(
                        authModel = it,
                        error = null,
                        isLoading = false
                    )
                }.onFailure {
                    _state.value = SignUpWithPasswordState(
                        authModel = null,
                        error = it,
                        isLoading = false
                    )
                }
            }
        } else {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = compoundError
                )
            }
        }
    }

    suspend fun subscribeToNotifications() {
        val userId = state.value.authModel?.userId ?: return
        _subscribeToChannelNotifications(userId)
    }
}