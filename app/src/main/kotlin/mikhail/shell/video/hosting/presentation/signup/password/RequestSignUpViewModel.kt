package mikhail.shell.video.hosting.presentation.signup.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.authentication.signup.RequestSignUpWithPassword
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateUserName
import javax.inject.Inject

@HiltViewModel
class RequestSignUpViewModel @Inject constructor(
    private val validateUserName: ValidateUserName,
    private val request: RequestSignUpWithPassword
) : ViewModel() {
    private val _state = MutableStateFlow(RequestSignUpScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: RequestSignUpUiEvent) {
        when (event) {
            RequestSignUpUiEvent.Submit -> request()
            is RequestSignUpUiEvent.UserNameChanged -> onUserNameChanged(event.userName)
        }
    }

    private fun onUserNameChanged(userName: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    userName = userName,
                    userNameError = userName.let {
                        val validationResult = validateUserName(userName)
                        if (validationResult is Result.Failure) {
                            validationResult.error
                        } else {
                            validationResult as Result.Success
                            if (validationResult.data) TextError.EXISTS else null
                        }
                    }
                )
            }
        }
    }

    private fun request() {
        viewModelScope.launch {
            request.invoke(_state.value.userName)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isAccepted = true,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error)
                    }
                }
        }
    }
}

sealed class RequestSignUpUiEvent {
    data class UserNameChanged(val userName: String): RequestSignUpUiEvent()
    data object Submit: RequestSignUpUiEvent()
}