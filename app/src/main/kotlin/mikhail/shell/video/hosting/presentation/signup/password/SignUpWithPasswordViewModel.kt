package mikhail.shell.video.hosting.presentation.signup.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    fun signUp(signUpInputState: SignUpInputState) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _signUpWithPassword(signUpInputState).onSuccess {
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
    }
}