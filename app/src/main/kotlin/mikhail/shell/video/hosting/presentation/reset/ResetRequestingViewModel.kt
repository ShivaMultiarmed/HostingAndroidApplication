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
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.RequestResetPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateUserName
import javax.inject.Inject
import mikhail.shell.video.hosting.presentation.reset.ResetRequestingScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.reset.ResetRequestingScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.reset.ResetRequestingScreenState as ScreenState

@HiltViewModel
class ResetRequestingViewModel @Inject constructor(
    private val validateUserName: ValidateUserName,
    private val requestResetPassword: RequestResetPassword
) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            ScreenAction.Submit -> request()
            is ScreenAction.UserNameChanged -> onUserNameChanged(action.userName)
            ScreenAction.UserNameFocused -> onUserNameFocused()
            ScreenAction.UserNameBlurred -> onUserNameBlurred()
            ScreenAction.Cancel -> viewModelScope.launch {
                _events.emit(ScreenEvent.NavigateBack)
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
                        _events.emit(ScreenEvent.Success(userId))
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
                            _events.emit(ScreenEvent.Failure(error))
                        }
                    }
                }
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }
}