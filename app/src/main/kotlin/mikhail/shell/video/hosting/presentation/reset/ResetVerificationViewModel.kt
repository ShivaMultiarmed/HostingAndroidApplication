package mikhail.shell.video.hosting.presentation.reset

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
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.RequestResetPassword
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.VerifyResetPassword
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.presentation.reset.ResetVerificationScreenState as ScreenState

@HiltViewModel(assistedFactory = ResetVerificationViewModel.Factory::class)
class ResetVerificationViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    @Assisted("userName") private val userName: String,
    private val validateCode: ValidateCode,
    private val requestResetPassword: RequestResetPassword,
    private val verifyResetPassword: VerifyResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ResetVerificationScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ResetVerificationScreenAction) {
        when (action) {
            is ResetVerificationScreenAction.CodeChanged -> onCodeChanged(action.code)
            ResetVerificationScreenAction.RequestCode -> requestCode()
            ResetVerificationScreenAction.Submit -> verify()
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
            requestResetPassword(userName).onFailure { error ->
                viewModelScope.launch {
                    _events.emit(ResetVerificationScreenEvent.Failure(error))
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
            verifyResetPassword(
                userId = userId,
                code = _state.value.code.value
            ).onSuccess { token ->
                viewModelScope.launch {
                    _events.emit(ResetVerificationScreenEvent.Success(token))
                }
            }.onFailure { error ->
                if (error is TextError) {
                    _state.update {
                        it.copy(
                            code = it.code.copy(error = error),
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(ResetVerificationScreenEvent.Failure(error))
                    }
                }
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: Long,
            @Assisted("userName") userName: String
        ): ResetVerificationViewModel
    }
}