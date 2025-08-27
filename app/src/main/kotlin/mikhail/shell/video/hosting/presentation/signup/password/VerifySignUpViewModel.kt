package mikhail.shell.video.hosting.presentation.signup.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.RequestSignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.VerifySignUpWithPassword

@HiltViewModel(assistedFactory = VerifySignUpViewModel.Factory::class)
class VerifySignUpViewModel @AssistedInject constructor(
    @Assisted("userName") private val userName: String,
    private val requestSignUpWithPassword: RequestSignUpWithPassword,
    private val verifySignUpWithPassword: VerifySignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow<VerifySignUpScreenState>(VerifySignUpScreenState.Entering())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.collect {
                if (it is VerifySignUpScreenState.Expired) {
                    _state.update {
                        VerifySignUpScreenState.Entering()
                    }
                    requestSignUpWithPassword(userName) // TODO
                }
            }
        }
    }

    fun onEvent(event: VerifySignUpUiEvent) {
        when (event) {
            is VerifySignUpUiEvent.CodeChanged -> onCodeChanged(event.code)
            VerifySignUpUiEvent.Submit -> verify()
        }
    }

    private fun onCodeChanged(code: String) {
        _state.update {
            it as VerifySignUpScreenState.Entering
            it.copy(code = code)
        }
    }

    private fun verify() {
        viewModelScope.launch {
            verifySignUpWithPassword(
                userName = userName,
                code = (_state.value as VerifySignUpScreenState.Entering).code
            ).onSuccess { token ->
                _state.update {
                    VerifySignUpScreenState.Success(token)
                }
            }.onFailure { error ->
                _state.update {
                    when (error) {
                        NetworkError.FORBIDDEN -> {
                            VerifySignUpScreenState.Expired
                        }
                        NetworkError.BAD_REQUEST -> {
                            it as VerifySignUpScreenState.Entering
                            it.copy(
                                codeError = TextError.NOT_CORRECT
                            )
                        }
                        else -> {
                            it as VerifySignUpScreenState.Entering
                            it.copy(
                                codeError = error
                            )
                        }
                    }
                }
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userName") userName: String): VerifySignUpViewModel
    }
}

sealed class VerifySignUpUiEvent {
    data object Submit: VerifySignUpUiEvent()
    data class CodeChanged(val code: String): VerifySignUpUiEvent()
}