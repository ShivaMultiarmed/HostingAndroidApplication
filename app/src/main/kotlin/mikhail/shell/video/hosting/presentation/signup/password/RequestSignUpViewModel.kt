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
import mikhail.shell.video.hosting.domain.usecases.authentication.RequestSignUpWithPassword
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

@HiltViewModel
class RequestSignUpViewModel @Inject constructor(
    private val _requestSignUpWithPassword: RequestSignUpWithPassword
): ViewModel() {
    private val _state = MutableStateFlow(RequestSignUpScreenState())
    val state = _state.asStateFlow()

    fun request(userName: String) {
        val compoundError = CompoundError<SignUpError>()
        if (userName.isEmpty()) {
            compoundError.add(SignUpError.USERNAME_EMPTY)
        } else if (!userName.matches(ValidationRules.EMAIL_REGEX)) {
            compoundError.add(SignUpError.USERNAME_MALFORMED)
        }
        if (compoundError.isNotEmpty()) {
            _state.update {
                it.copy(error = compoundError)
            }
        } else {
            viewModelScope.launch {
                _requestSignUpWithPassword(userName)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                isAccepted = true,
                                error = null
                            )
                        }
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(error = error)
                        }
                    }
            }
        }
    }

}