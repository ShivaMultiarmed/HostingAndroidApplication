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
import mikhail.shell.video.hosting.domain.errors.SignUpError.EMAIL_EMPTY
import mikhail.shell.video.hosting.domain.errors.SignUpError.EMAIL_INVALID
import mikhail.shell.video.hosting.domain.errors.SignUpError.NAME_EMPTY
import mikhail.shell.video.hosting.domain.errors.SignUpError.PASSWORD_EMPTY
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.usecases.authentication.SignUpWithPassword
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import javax.inject.Inject

@HiltViewModel
class SignUpWithPasswordViewModel @Inject constructor(
    private val _signUpWithPassword: SignUpWithPassword
): ViewModel() {
    private val _state = MutableStateFlow(SignUpWithPasswordState())
    val state = _state.asStateFlow()
    companion object {
        private val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    }
    private fun validateSignUpInput(inputState: SignUpInputState): CompoundError<SignUpError>? {
        val error = CompoundError<SignUpError>()
        if (inputState.userName == "")
            error.add(EMAIL_EMPTY)
        else if (!emailRegex.matches(inputState.userName))
            error.add(EMAIL_INVALID)
        if (inputState.password == "")
            error.add(PASSWORD_EMPTY)
        if (inputState.name == "")
            error.add(NAME_EMPTY)
        return if (error.isNotNull())
            error
        else
            null
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
                name = signUpInputState.name
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
}