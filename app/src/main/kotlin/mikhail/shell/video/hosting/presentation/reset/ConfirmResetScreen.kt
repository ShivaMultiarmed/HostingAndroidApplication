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
import androidx.compose.material.icons.rounded.Password
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
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun ConfirmResetScreen(
    state: ResetConfirmationScreenState,
    onAction: (ResetConfirmationAction) -> Unit,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            Title(
                text = stringResource(R.string.reset_password_title)
            )
            val passwordErrMsg = when (state.user.password.error) {
                TextError.EMPTY -> stringResource(R.string.password_empty_error)
                TextError.LONG -> stringResource(R.string.password_too_long)
                TextError.SHORT -> stringResource(R.string.password_too_short)
                TextError.PATTERN -> stringResource(R.string.password_not_valid_error)
                else -> null
            }
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Password,
                value = state.user.password.value,
                onValueChange = {
                    onAction(ResetConfirmationAction.PasswordChanged(it))
                },
                errorMsg = passwordErrMsg,
                secured = true,
                label = stringResource(R.string.password_label),
                onFocus = {
                    onAction(ResetConfirmationAction.PasswordFocused)
                },
                onBlur = {
                    onAction(ResetConfirmationAction.PasswordBlurred)
                }
            )
            val passwordDuplicateErrorMsg = when (state.user.passwordDuplicate.error) {
                TextError.PATTERN -> stringResource(R.string.passwords_not_match)
                else -> null
            }
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Password,
                value = state.user.passwordDuplicate.value,
                onValueChange = {
                    onAction(ResetConfirmationAction.PasswordDuplicatedChanged(it))
                },
                errorMsg = passwordDuplicateErrorMsg,
                secured = true,
                label = stringResource(R.string.password_again_label),
                onFocus = {
                    onAction(ResetConfirmationAction.PasswordDuplicatedFocused)
                },
                onBlur = {
                    onAction(ResetConfirmationAction.PasswordDuplicatedBlurred)
                }
            )
            PrimaryProgressButton(
                inProgress = state.isLoading,
                onClick = {
                    onAction(ResetConfirmationAction.Submit)
                },
                text = stringResource(R.string.reset_password_button)
            )
        }
    }

}