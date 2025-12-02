package mikhail.shell.video.hosting.presentation.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.RequestResetPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateUserName
import javax.inject.Inject

@HiltViewModel
class ResetRequestingViewModel @Inject constructor(
    private val validateUserName: ValidateUserName,
    private val requestResetPassword: RequestResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ResetRequestingScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ResetRequestingEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ResetRequestingAction) {
        when (action) {
            ResetRequestingAction.Submit -> request()
            is ResetRequestingAction.UserNameChanged -> onUserNameChanged(action.userName)
            ResetRequestingAction.UserNameFocused -> onUserNameFocused()
            ResetRequestingAction.UserNameBlurred -> onUserNameBlurred()
            ResetRequestingAction.Cancel -> viewModelScope.launch {
                _events.emit(
                    ResetRequestingEvent.NavigateBack
                )
            }
        }
    }

    private fun onUserNameChanged(userName: String) {
        _state.update {
            it.copy(
                userName = it.userName.copy(value = userName)
            )
        }
    }

    private fun onUserNameFocused() {
        _state.update {
            it.copy(
                userName = it.userName.copy(error = null)
            )
        }
    }

    private fun onUserNameBlurred() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    userName = it.userName.copy(
                        error = validateUserName(
                            UserNameCheckPurpose.RESET,
                            it.userName.value
                        ).errorOrNull()
                    )
                )
            }
        }
    }

    private fun request() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    userName = it.userName.copy(
                        error = validateUserName(
                            UserNameCheckPurpose.RESET,
                            it.userName.value
                        ).errorOrNull()
                    )
                )
            }
            if (_state.value.userName.error != null) {
                return@launch
            }
            _state.update {
                it.copy(isLoading = true)
            }
            requestResetPassword(_state.value.userName.value)
                .onSuccess { userId ->
                    viewModelScope.launch {
                        _events.emit(ResetRequestingEvent.Success(userId))
                    }
                }.onFailure { error ->
                    if (error is TextError) {
                        _state.update {
                            it.copy(
                                userName = it.userName.copy(error = error)
                            )
                        }
                    } else {
                        viewModelScope.launch {
                            _events.emit(ResetRequestingEvent.Failure(error))
                        }
                    }
                }
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }
}

sealed class ResetRequestingAction {
    data class UserNameChanged(val userName: String) : ResetRequestingAction()
    data object UserNameFocused : ResetRequestingAction()
    data object UserNameBlurred : ResetRequestingAction()
    data object Submit : ResetRequestingAction()
    data object Cancel : ResetRequestingAction()
}

sealed class ResetRequestingEvent {
    data class Success(val userId: Long) : ResetRequestingEvent()
    data class Failure(val error: Error) : ResetRequestingEvent()
    data object NavigateBack : ResetRequestingEvent()
}