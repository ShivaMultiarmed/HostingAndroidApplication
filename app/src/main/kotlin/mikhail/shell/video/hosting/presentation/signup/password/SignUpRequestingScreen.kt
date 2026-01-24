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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_USERNAME_LENGTH
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.signup.password.SignUpRequestingScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.signup.password.SignUpRequestingScreenState as ScreenState

@Composable
internal fun SignUpRequestingScreen(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
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
            val userNameErrorMsg = when(state.userName.error) {
                TextError.EMPTY -> stringResource(R.string.user_name_empty_error)
                TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_USERNAME_LENGTH)
                TextError.PATTERN -> stringResource(R.string.email_malformed_error)
                TextError.EXISTS -> stringResource(R.string.email_exists_msg_error)
                UnexpectedError -> stringResource(R.string.unexpected_error)
                else -> null
            }
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Email,
                value = state.userName.value,
                onValueChange = {
                    onAction(ScreenAction.UserNameChanged(it))
                },
                errorMsg = userNameErrorMsg,
                label = "E-mail",
                onFocus = {
                    onAction(ScreenAction.UserNameFocused)
                },
                onBlur = {
                    onAction(ScreenAction.UserNameBlurred)
                }
            )
            PrimaryProgressButton(
                inProgress = state.isLoading,
                onClick = {
                    onAction(ScreenAction.Submit)
                },
                text = stringResource(R.string.sign_up_main_button)
            )
        }
    }
}