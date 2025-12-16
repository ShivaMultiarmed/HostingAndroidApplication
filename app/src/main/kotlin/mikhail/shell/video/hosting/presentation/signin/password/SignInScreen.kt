package mikhail.shell.video.hosting.presentation.signin.password

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreenState as ScreenState

@Composable
fun SignInScreen(
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
            SnackbarHost(
                hostState = snackBarHostState
            )
        }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            Title(
                text = stringResource(R.string.sign_in_title)
            )
            val emailErrorMsg = when (state.input.userName.error) {
                is TextError -> when (state.input.userName.error) {
                    TextError.EMPTY -> stringResource(R.string.user_name_empty_error)
                    TextError.LONG -> stringResource(R.string.user_name_too_long)
                    TextError.PATTERN -> stringResource(R.string.email_malformed_error)
                    TextError.NOT_EXISTS -> stringResource(R.string.email_not_found_error)
                    else -> null
                }
                is NetworkError -> stringResource(R.string.user_name_check_failed)
                else -> null
            }
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Email,
                label = stringResource(R.string.email_label),
                value = state.input.userName.value,
                onValueChange = {
                    onAction(ScreenAction.UserNameChanged(it))
                },
                errorMsg = emailErrorMsg,
                onFocus = {
                    onAction(ScreenAction.UserNameFocused)
                },
                onBlur = {
                    onAction(ScreenAction.UserNameBlurred)
                }
            )
            val passwordErrorMsg = when (state.input.password.error) {
                TextError.EMPTY -> stringResource(R.string.password_empty_error)
                TextError.SHORT -> stringResource(R.string.password_too_short)
                TextError.LONG -> stringResource(R.string.password_too_long)
                TextError.PATTERN -> stringResource(R.string.password_not_valid_error)
                TextError.NOT_CORRECT -> stringResource(R.string.password_incorrect_error)
                else -> null
            }
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Password,
                label = stringResource(R.string.password_label),
                value = state.input.password.value,
                onValueChange = {
                    onAction(ScreenAction.PasswordChanged(it))
                },
                onFocus = {
                    onAction(ScreenAction.PasswordFocused)
                },
                onBlur = {
                    onAction(ScreenAction.PasswordBlurred)
                },
                secured = true,
                errorMsg = passwordErrorMsg
            )
            Text(
                modifier = Modifier.clickable {
                    onAction(ScreenAction.ResetPassword)
                },
                text = stringResource(R.string.forgot_password),
                fontSize = 12.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PrimaryProgressButton(
                    inProgress = state.isLoading,
                    onClick = {
                        onAction(ScreenAction.Submit)
                    },
                    text = stringResource(R.string.sign_in_main_btn_label)
                )
                Text(
                    text = stringResource(R.string.sign_up_link_label),
                    modifier = Modifier.clickable {
                        onAction(ScreenAction.SignUp)
                    }
                )
            }
        }
    }
}