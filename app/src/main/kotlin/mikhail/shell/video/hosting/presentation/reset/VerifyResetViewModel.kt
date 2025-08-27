package mikhail.shell.video.hosting.presentation.reset

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
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.VerifyResetPassword
import mikhail.shell.video.hosting.domain.validation.ValidationRules

@HiltViewModel(assistedFactory = VerifyResetViewModel.Factory::class)
class VerifyResetViewModel @AssistedInject constructor(
    @Assisted("userName") private val userName: String,
    private val verifyResetPassword: VerifyResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow<VerifyResetScreenState>(VerifyResetScreenState.Entering())
    val state = _state.asStateFlow()

    fun onEvent(event: VerifyResetUiEvent) {
        when (event) {
            is VerifyResetUiEvent.CodeChanged -> onCodeChanged(event.code)
        }
    }

    private fun onCodeChanged(code: String) {
        _state.update {
            it as VerifyResetScreenState.Entering
            it.copy(
                code = code
            )
        }
        if (code.length == ValidationRules.CODE_LENGTH) {
            verify()
        }
    }

    private fun verify() {
        _state.update {
            it as VerifyResetScreenState.Entering
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            verifyResetPassword(
                userName = userName,
                code = (_state.value as VerifyResetScreenState.Entering).code
            ).onSuccess { token ->
                _state.update {
                    VerifyResetScreenState.Success(token)
                }
            }.onFailure { error ->
                _state.update {
                    it as VerifyResetScreenState.Entering
                    it.copy(
                        error = error,
                        isLoading = false
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userName") userName: String): VerifyResetViewModel
    }
}

sealed class VerifyResetUiEvent {
    data class CodeChanged(val code: String): VerifyResetUiEvent()
}