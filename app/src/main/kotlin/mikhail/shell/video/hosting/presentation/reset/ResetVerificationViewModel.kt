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
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.VerifyResetPassword
import mikhail.shell.video.hosting.domain.validation.ValidationRules

@HiltViewModel(assistedFactory = ResetVerificationViewModel.Factory::class)
class ResetVerificationViewModel @AssistedInject constructor(
    @Assisted("userName") private val userName: String,
    private val verifyResetPassword: VerifyResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ResetVerificationState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ResetVerificationEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ResetVerificationAction) {
        when (action) {
            is ResetVerificationAction.CodeChanged -> onCodeChanged(action.code)
        }
    }

    private fun onCodeChanged(code: String) {
        _state.update {
            it.copy(
                code = it.code.copy(value = code)
            )
        }
        if (code.length == ValidationRules.CODE_LENGTH) {
            verify()
        }
    }

    private fun verify() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            verifyResetPassword(
                userName = userName,
                code = _state.value.code.value
            ).onSuccess { token ->
                viewModelScope.launch {
                    _events.emit(ResetVerificationEvent.Success(token))
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
                        _events.emit(ResetVerificationEvent.Failure(error))
                    }
                }
            }
            _state.update {
                it.copy(isLoading = true)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userName") userName: String): ResetVerificationViewModel
    }
}

sealed class ResetVerificationAction {
    data class CodeChanged(val code: String): ResetVerificationAction()
}

sealed class ResetVerificationEvent {
    data class Success(val token: String): ResetVerificationEvent()
    data class Failure(val error: Error): ResetVerificationEvent()
}