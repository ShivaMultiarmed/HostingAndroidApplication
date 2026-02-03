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
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.authentication.ValidateCode
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.RequestSignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.VerifySignUpWithPassword
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationScreenState as ScreenState

@HiltViewModel(assistedFactory = SignUpVerificationViewModel.Factory::class)
class SignUpVerificationViewModel @AssistedInject constructor(
    @Assisted("userName") private val userName: String,
    private val validateCode: ValidateCode,
    private val requestSignUpWithPassword: RequestSignUpWithPassword,
    private val verifySignUpWithPassword: VerifySignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            is ScreenAction.CodeChanged -> onCodeChanged(action.code)
            ScreenAction.RequestCode -> requestCode()
            ScreenAction.Submit -> verify()
        }
    }

    private fun onCodeChanged(code: String) {
        _state.update {
            it.copy(
                code = it.code.copy(value = code)
            )
        }
        if (_state.value.code.value.length == ValidationRules.CODE_LENGTH) {
            verify()
        }
    }

    private fun requestCode() {
        if (_state.value.isRequestingCode) {
            return
        }
        _state.update {
            it.copy(isRequestingCode = true)
        }
        viewModelScope.launch {
            requestSignUpWithPassword(userName).onFailure { error ->
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Failure(error))
                }
            }
            _state.update {
                it.copy(isRequestingCode = false)
            }
        }
    }

    private fun verify() {
        if (_state.value.isLoading) {
            return
        }
        _state.update {
            it.copy(
                code = it.code.copy(
                    error = validateCode(it.code.value).errorOrNull()
                )
            )
        }
        if (_state.value.code.error != null) {
            return
        }
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            verifySignUpWithPassword(
                userName = userName,
                code = _state.value.code.value
            ).onSuccess { token ->
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Success(token))
                }
            }.onFailure { error ->
                if (error is TextError) {
                    _state.update {
                        it.copy(
                            code = it.code.copy(error = error),
                            isLoading = false
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(ScreenEvent.Failure(error))
                    }
                    _state.update {
                        it.copy(isLoading = false)
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