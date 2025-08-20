package mikhail.shell.video.hosting.presentation.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.authentication.ResetError
import mikhail.shell.video.hosting.domain.usecases.authentication.ConfirmResetPassword
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ConfirmResetViewModel @Inject constructor(
    private val _confirmResetPassword: ConfirmResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ConfirmResetScreenState())
    val state = _state.asStateFlow()

    fun confirm(
        token: String,
        password: String,
        passwordDuplicate: String
    ) {
        val compoundError = CompoundError<ResetError>()
        if (password.isEmpty()) {
            compoundError.add(ResetError.PASSWORD_EMPTY)
        } else if (!password.matches(ValidationRules.PASSWORD_REGEX)) {
            compoundError.add(ResetError.PASSWORD_NOT_VALID)
        }
        if (password != passwordDuplicate) {
            compoundError.add(ResetError.PASSWORDS_NOT_MATCH)
        }
        if (compoundError.isEmpty()) {
            _state.update {
                it.copy(error = compoundError)
            }
        } else {
            _state.update {
                it.copy(isLoading = true)
            }
            viewModelScope.launch {
                _confirmResetPassword(
                    token = token,
                    password = password
                ).onSuccess {
                    _state.update {
                        it.copy(
                            isAccepted = true,
                            isLoading = false,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            error = error,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}