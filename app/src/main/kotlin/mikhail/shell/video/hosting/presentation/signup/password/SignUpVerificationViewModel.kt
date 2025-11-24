package mikhail.shell.video.hosting.presentation.signup.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.RequestSignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.VerifySignUpWithPassword

@HiltViewModel(assistedFactory = SignUpVerificationViewModel.Factory::class)
class SignUpVerificationViewModel @AssistedInject constructor(
    @Assisted("userName") private val userName: String,
    private val requestSignUpWithPassword: RequestSignUpWithPassword,
    private val verifySignUpWithPassword: VerifySignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow<SignUpVerificationScreenState>(SignUpVerificationScreenState())
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<SignUpVerificationEvent>()
    val events = _events.asSharedFlow()

    fun onAction(event: SignUpVerificationAction) {
        when (event) {
            is SignUpVerificationAction.CodeChanged -> onCodeChanged(event.code)
            SignUpVerificationAction.Submit -> verify()
        }
    }

    private fun onCodeChanged(code: String) {
        _state.update {
            it.copy(
                code = it.code.copy(value = code)
            )
        }
    }

    private fun verify() {
        viewModelScope.launch {
            verifySignUpWithPassword(
                userName = userName,
                code = _state.value.code.value
            ).onSuccess { token ->
                viewModelScope.launch {
                    _events.emit(SignUpVerificationEvent.Success(token))
                }
            }.onFailure { error ->
                if (error is TextError) {
                    _state.update {
                        it.copy(
                            code = it.code.copy(error = error)
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(SignUpVerificationEvent.Failure(error))
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userName") userName: String): SignUpVerificationViewModel
    }
}

sealed class SignUpVerificationAction {
    data object Submit : SignUpVerificationAction()
    data class CodeChanged(val code: String) : SignUpVerificationAction()
}