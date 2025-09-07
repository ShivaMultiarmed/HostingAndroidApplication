package mikhail.shell.video.hosting.presentation.reset

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
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun RequestResetScreen(
    state: RequestResetScreenState,
    onEvent: (RequestResetUiEvent) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    if (state is RequestResetScreenState.Entering) {
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
                    text = stringResource(R.string.reset_password_title)
                )
                val userNameErrorMsg = when (state.userNameError) {
                    is TextError -> when (state.userNameError) {
                        TextError.LONG -> stringResource(R.string.user_name_too_long)
                        TextError.EMPTY -> stringResource(R.string.user_name_empty_error)
                        TextError.NOT_EXISTS -> stringResource(R.string.user_not_found)
                        TextError.PATTERN -> stringResource(R.string.user_name_malformed)
                        else -> null
                    }
                    else -> null
                }
                InputField(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    icon = Icons.Rounded.Email,
                    value = state.userName,
                    onValueChange = {
                        onEvent(RequestResetUiEvent.UserNameChanged(it))
                    },
                    errorMsg = userNameErrorMsg,
                    placeholder = "E-mail",
                    onTypingStarted = {
                        onEvent(RequestResetUiEvent.UserNameTypingStarted)
                    },
                    onTypingEnded = {
                        onEvent(RequestResetUiEvent.UserNameTypingEnded)
                    }
                )
                PrimaryProgressButton(
                    inProgress = state.isLoading,
                    onClick = {
                        onEvent(RequestResetUiEvent.Submit)
                    },
                    text = stringResource(R.string.go_forward_button)
                )
                StandardComplexErrorHandler(
                    error = state.error,
                    snackBarHostState = snackBarHostState
                )
            }
        }
    }
}