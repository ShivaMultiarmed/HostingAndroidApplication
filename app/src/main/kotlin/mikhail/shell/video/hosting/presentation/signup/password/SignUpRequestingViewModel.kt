package mikhail.shell.video.hosting.presentation.signup.password

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
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.RequestSignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateUserName
import javax.inject.Inject

@HiltViewModel
class SignUpRequestingViewModel @Inject constructor(
    private val validateUserName: ValidateUserName,
    private val request: RequestSignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpRequestingScreenState())
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<SignUpRequestingEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: SignUpRequestingAction) {
        when (action) {
            SignUpRequestingAction.Submit -> request()
            is SignUpRequestingAction.UserNameChanged -> onUserNameChanged(action.userName)
            SignUpRequestingAction.UserNameFocused -> onUserNameFocused()
            SignUpRequestingAction.UserNameBlurred -> onUserNameBlurred()
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
                        error = validateUserName(UserNameCheckPurpose.SIGN_UP, it.userName.value).errorOrNull()
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
                        error = validateUserName(UserNameCheckPurpose.SIGN_UP, it.userName.value).errorOrNull()
                    )
                )
            }
            if (_state.value.userName.error != null) {
                return@launch
            }
            request.invoke(_state.value.userName.value).onSuccess {
                viewModelScope.launch {
                    _events.emit(SignUpRequestingEvent.Success(_state.value.userName.value))
                }
            }.onFailure { error ->
                if (error is TextError) {
                    _state.update {
                        it.copy(
                            userName = it.userName.copy(
                                error = error
                            )
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _events.emit(SignUpRequestingEvent.Failure(error))
                    }
                }
            }
        }
    }
}

sealed class SignUpRequestingAction {
    data class UserNameChanged(val userName: String) : SignUpRequestingAction()
    data object UserNameFocused : SignUpRequestingAction()
    data object UserNameBlurred : SignUpRequestingAction()
    data object Submit : SignUpRequestingAction()
}

sealed class SignUpRequestingEvent {
    data class Success(val userName: String) : SignUpRequestingEvent()
    data class Failure(val error: Error) : SignUpRequestingEvent()
    data object Cancel : SignUpRequestingEvent()
}