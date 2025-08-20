package mikhail.shell.video.hosting.presentation.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.authentication.VerifyResetPassword
import javax.inject.Inject

class VerifyResetViewModel @Inject constructor(
    private val _verifyResetPassword: VerifyResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow(VerifyResetScreenState())
    val state = _state.asStateFlow()

    fun verify(userName: String, code: String) {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            _verifyResetPassword(userName, code)
                .onSuccess { token ->
                    _state.update {
                        it.copy(
                            token = token,
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