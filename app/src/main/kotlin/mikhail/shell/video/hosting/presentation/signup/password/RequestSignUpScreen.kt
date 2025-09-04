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
import androidx.compose.material.icons.rounded.Email
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
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_USERNAME_LENGTH
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun RequestSignUpScreen(
    state: RequestSignUpScreenState,
    onEvent: (RequestSignUpUiEvent) -> Unit
) {
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
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            Title(
                text = stringResource(R.string.sign_up_title)
            )
            val userNameErrorMsg = when(state.userNameError) {
                TextError.EMPTY -> stringResource(R.string.user_name_empty_error)
                TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_USERNAME_LENGTH)
                TextError.EXISTS -> stringResource(R.string.email_exists_msg_error)
                TextError.PATTERN -> stringResource(R.string.email_malformed_error)
                else -> null
            }
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Email,
                value = state.userName,
                onValueChange = {
                    onEvent(RequestSignUpUiEvent.UserNameChanged(it))
                },
                errorMsg = userNameErrorMsg,
                placeholder = "E-mail",
                onTypingStarted = {
                    onEvent(RequestSignUpUiEvent.UserNameTypingStarted)
                },
                onTypingEnded = {
                    onEvent(RequestSignUpUiEvent.UserNameTypingEnded)
                }
            )
            PrimaryProgressButton(
                inProgress = state.isLoading,
                complete = state.isAccepted,
                onClick = {
                    onEvent(RequestSignUpUiEvent.Submit)
                },
                text = stringResource(R.string.sign_up_main_button)
            )
            StandardComplexErrorHandler(
                error = state.error,
                snackBarHostState = snackBarHostState
            )
        }
    }
}