package mikhail.shell.video.hosting.presentation.signup.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.authentication.VerifySignUpWithPassword
import javax.inject.Inject

@HiltViewModel
class VerifySignUpViewModel @Inject constructor(
    private val _verifySignUpWithPassword: VerifySignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow(VerifySignUpScreenState())
    val state = _state.asStateFlow()

    fun verify(userName: String, code: String) {
        viewModelScope.launch {
            _verifySignUpWithPassword(
                userName = userName,
                code = code
            ).onSuccess { token ->
                _state.update {
                    it.copy(
                        token = token,
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