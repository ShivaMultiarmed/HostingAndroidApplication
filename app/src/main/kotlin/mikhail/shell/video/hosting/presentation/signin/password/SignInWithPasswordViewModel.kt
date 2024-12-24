package mikhail.shell.video.hosting.presentation.signin.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.authentication.SignInWithEmailAndPassword
import javax.inject.Inject

@HiltViewModel
class SignInWithPasswordViewModel @Inject constructor(
    private val _signInWithEmailAndPassword: SignInWithEmailAndPassword
) : ViewModel() {
    private val _state = MutableStateFlow(SignInWithPasswordState())
    val state = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _signInWithEmailAndPassword(
                email,
                password
            ).onSuccess {
                _state.value = SignInWithPasswordState(
                    isLoading = false,
                    authModel = it,
                    error = null
                )
            }.onFailure {
                _state.value = SignInWithPasswordState(
                    isLoading = false,
                    authModel = null,
                    error = it
                )
            }
        }
    }
}