package mikhail.shell.video.hosting.presentation.signup.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError.NICK_EMPTY
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError.PASSWORDS_NOT_MATCH
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError.PASSWORD_EMPTY
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.usecases.authentication.ConfirmSignUpWithPassword
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import javax.inject.Inject

@HiltViewModel
class ConfirmSignUpViewModel @Inject constructor(
    private val _confirmSignUpWithPassword: ConfirmSignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ConfirmSignUpScreenState())
    val state = _state.asStateFlow()

    private fun validate(inputState: SignUpInputState): CompoundError<SignUpError>? {
        val error = CompoundError<SignUpError>()
        if (inputState.password.isEmpty()) {
            error.add(PASSWORD_EMPTY)
        } else if (inputState.password != inputState.passwordDuplicate) {
            error.add(PASSWORDS_NOT_MATCH)
        }
        if (inputState.nick.isEmpty()) {
            error.add(NICK_EMPTY)
        }
        return error.takeIf { it.isNotEmpty() }
    }

    fun confirm(token: String, input: SignUpInputState) {
        val error = validate(input)
        if (error != null) {
            _state.update {
                it.copy(
                    error = error
                )
            }
        } else {
            val user = User(
                nick = input.nick
            )
            viewModelScope.launch {
                _confirmSignUpWithPassword(
                    token = token,
                    password = input.password,
                    user = user
                ).onSuccess { authModel ->
                    _state.update {
                        it.copy(
                            authModel = authModel,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            error = error
                        )
                    }
                }
            }
        }
    }
}