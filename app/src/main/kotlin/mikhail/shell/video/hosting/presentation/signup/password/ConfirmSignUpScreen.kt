package mikhail.shell.video.hosting.presentation.signup.password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun ConfirmSignUpScreen(
    state: ConfirmSignUpScreenState,
    onEvent: (ConfirmSignUpUiEvent) -> Unit
) {
    if (state is ConfirmSignUpScreenState.Entering) {
        val snackBarHostState = remember { SnackbarHostState() }
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackBarHostState
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                Title(
                    text = stringResource(R.string.sign_up_title)
                )
                val passwordErrorMsg = when (state.input.passwordError) {
                    TextError.LONG -> TODO()
                    TextError.EMPTY -> stringResource(R.string.password_empty_error)
                    TextError.SHORT -> TODO()
                    TextError.PATTERN -> stringResource(R.string.password_not_valid_error)
                    else -> null
                }
                InputField(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    icon = Icons.Rounded.Password,
                    value = state.input.password,
                    onValueChange = {
                        onEvent(ConfirmSignUpUiEvent.PasswordChanged(it))
                    },
                    errorMsg = passwordErrorMsg,
                    secured = true,
                    placeholder = stringResource(R.string.password_label),
                    onTypingStarted = {
                        onEvent(ConfirmSignUpUiEvent.PasswordTypingStarted)
                    },
                    onTypingEnded = {
                        onEvent(ConfirmSignUpUiEvent.PasswordTypingStarted)
                    }
                )
                val passwordDuplicateErrorMsg = constructInfoMessage( // TODO
                    state.error,
                    mapOf(
                        SignUpError.PASSWORDS_NOT_MATCH to stringResource(R.string.passwords_not_match)
                    )
                )
                InputField(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    icon = Icons.Rounded.Password,
                    value = state.input.passwordDuplicate,
                    onValueChange = {
                        onEvent(ConfirmSignUpUiEvent.PasswordDuplicateChanged(it))
                    },
                    errorMsg = passwordDuplicateErrorMsg,
                    secured = true,
                    placeholder = stringResource(R.string.password_again_label),
                    onTypingStarted = {
                        onEvent(ConfirmSignUpUiEvent.PasswordDuplicateTypingStarted)
                    },
                    onTypingEnded = {
                        onEvent(ConfirmSignUpUiEvent.PasswordDuplicateTypingEnded)
                    }
                )
                val nickErrMsg = constructInfoMessage( // TODO
                    error = state.error,
                    errorMessages = mapOf(
                        SignUpError.NICK_EMPTY to stringResource(R.string.nick_empty_error),
                        SignUpError.NICK_TOO_LARGE to stringResource(
                            R.string.text_too_large_error,
                            ValidationRules.MAX_NAME_LENGTH
                        ),
                        SignUpError.NICK_EXISTS to stringResource(R.string.nick_exists_error)
                    )
                )
                InputField(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    icon = Icons.Rounded.Person,
                    value = state.input.nick,
                    onValueChange = {
                        onEvent(ConfirmSignUpUiEvent.NickChanged(it))
                    },
                    errorMsg = nickErrMsg,
                    placeholder = stringResource(R.string.nick_label),
                    onTypingStarted = {
                        onEvent(ConfirmSignUpUiEvent.PasswordDuplicateTypingStarted)
                    },
                    onTypingEnded = {
                        onEvent(ConfirmSignUpUiEvent.PasswordDuplicateTypingEnded)
                    }
                )
                PrimaryProgressButton(
                    inProgress = state.isLoading,
                    onClick = {
                        onEvent(ConfirmSignUpUiEvent.Submit)
                    },
                    text = stringResource(R.string.sign_up_main_button)
                )
            }
            StandardComplexErrorHandler(
                error = state.error,
                snackBarHostState = snackBarHostState
            )
        }
    }
}