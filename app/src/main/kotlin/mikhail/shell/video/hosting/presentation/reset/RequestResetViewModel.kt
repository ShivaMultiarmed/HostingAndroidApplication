package mikhail.shell.video.hosting.presentation.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.authentication.reset.RequestResetPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateUserName
import javax.inject.Inject

@HiltViewModel
class RequestResetViewModel @Inject constructor(
    private val validateUserName: ValidateUserName,
    private val requestResetPassword: RequestResetPassword
) : ViewModel() {
    private val _state =
        MutableStateFlow<RequestResetScreenState>(RequestResetScreenState.Entering())
    val state = _state.asStateFlow()

    fun onEvent(event: RequestResetUiEvent) {
        when (event) {
            RequestResetUiEvent.Submit -> request()
            is RequestResetUiEvent.UserNameChanged -> onUserNameChanged(event.userName)
            RequestResetUiEvent.UserNameTypingStarted -> onUserNameTypingStarted()
            RequestResetUiEvent.UserNameTypingEnded -> onUserNameTypingEnded()
            else -> Unit
        }
    }

    private fun onUserNameChanged(userName: String) {
        viewModelScope.launch {
            _state.update {
                it as RequestResetScreenState.Entering
                it.copy(
                    userName = userName
                )
            }
        }
    }
    private fun onUserNameTypingStarted() {
        viewModelScope.launch {
            _state.update {
                it as RequestResetScreenState.Entering
                it.copy(
                    userNameError = null
                )
            }
        }
    }
    private fun onUserNameTypingEnded() {
        viewModelScope.launch {
            _state.update {
                it as RequestResetScreenState.Entering
                it.copy(
                    userNameError = it.userName.let {
                        val validationResult = validateUserName(it)
                        if (validationResult is Result.Failure<TextError>) {
                            validationResult.error
                        } else {
                            validationResult as Result.Success<Boolean>
                            if (!validationResult.data) {
                                TextError.NOT_EXISTS
                            } else {
                                null
                            }
                        }
                    }
                )
            }
        }
    }

    private fun request() {
        _state.update {
            it as RequestResetScreenState.Entering
            it.copy(isLoading = true)
        }
        val currentInput = (_state.value as RequestResetScreenState.Entering)
        viewModelScope.launch {
            requestResetPassword(currentInput.userName)
                .onSuccess {
                    _state.update {
                        RequestResetScreenState.Success(currentInput.userName)
                    }
                }.onFailure { error ->
                    _state.update {
                        it as RequestResetScreenState.Entering
                        it.copy(
                            error = error,
                            isLoading = false
                        )
                    }
                }
        }
    }
}

sealed class RequestResetUiEvent {
    data class UserNameChanged(val userName: String): RequestResetUiEvent()
    data object UserNameTypingStarted: RequestResetUiEvent()
    data object UserNameTypingEnded: RequestResetUiEvent()
    data object Submit: RequestResetUiEvent()
    data object Cancel: RequestResetUiEvent()
}