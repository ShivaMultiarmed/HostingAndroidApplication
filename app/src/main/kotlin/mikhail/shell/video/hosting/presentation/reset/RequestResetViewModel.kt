package mikhail.shell.video.hosting.presentation.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.authentication.ResetError
import mikhail.shell.video.hosting.domain.usecases.authentication.RequestResetPassword
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

@HiltViewModel
class RequestResetViewModel @Inject constructor(
    private val _requestResetPassword: RequestResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow(RequestResetScreenState())
    val state = _state.asStateFlow()

    fun request(userName: String) {
        if (userName.isEmpty()) {
            _state.update {
                it.copy(error = ResetError.USERNAME_EMPTY)
            }
        } else if (!userName.matches(ValidationRules.EMAIL_REGEX)) {
            _state.update {
                it.copy(error = ResetError.USERNAME_MALFORMED)
            }
        } else {
            _state.update {
                it.copy(isLoading = true)
            }
            viewModelScope.launch {
                _requestResetPassword(userName)
                    .onSuccess {
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