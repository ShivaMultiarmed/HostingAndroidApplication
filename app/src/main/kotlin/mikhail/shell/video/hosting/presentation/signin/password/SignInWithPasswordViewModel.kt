package mikhail.shell.video.hosting.presentation.signin.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.domain.usecases.authentication.SignInWithPassword
import mikhail.shell.video.hosting.domain.usecases.channels.SubscribeToChannelNotifications
import javax.inject.Inject

@HiltViewModel
class SignInWithPasswordViewModel @Inject constructor(
    private val _signInWithPassword: SignInWithPassword,
    private val _subscribeToChannelNotifications: SubscribeToChannelNotifications
) : ViewModel() {
    private val _state = MutableStateFlow(SignInWithPasswordState())
    val state = _state.asStateFlow()

    companion object {
        private val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    }

    private fun validateSignInInput(email: String, password: String): CompoundError<SignInError>? {
        val compoundError = CompoundError<SignInError>()
        if (email.isEmpty())
            compoundError.add(SignInError.USERNAME_EMPTY)
        else if (!emailRegex.matches(email))
            compoundError.add(SignInError.USERNAME_MALFORMED)
        if (password.isEmpty())
            compoundError.add(SignInError.PASSWORD_EMPTY)
        return if (compoundError.isNull()) null else compoundError
    }

    fun signIn(email: String, password: String) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        val compoundError = validateSignInInput(email, password)
        if (compoundError == null) {
            viewModelScope.launch {
                _signInWithPassword(
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