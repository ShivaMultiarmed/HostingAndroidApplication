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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_NAME_LENGTH
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
internal fun SignUpConfirmationScreen(
    state: SignUpConfirmationState,
    onAction: (SignUpConfirmationAction) -> Unit,
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
                text = stringResource(R.string.sign_up_title)
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
                    onAction(SignUpConfirmationAction.PasswordChanged(it))
                },
                errorMsg = passwordErrMsg,
                secured = true,
                label = stringResource(R.string.password_label),
                onFocus = {
                    onAction(SignUpConfirmationAction.PasswordFocused)
                },
                onBlur = {
                    onAction(SignUpConfirmationAction.PasswordBlurred)
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
                    onAction(SignUpConfirmationAction.PasswordDuplicateChanged(it))
                },
                errorMsg = passwordDuplicateErrorMsg,
                secured = true,
                label = stringResource(R.string.password_again_label),
                onFocus = {
                    onAction(SignUpConfirmationAction.PasswordDuplicateFocused)
                },
                onBlur = {
                    onAction(SignUpConfirmationAction.PasswordDuplicateBlurred)
                }
            )
            val nickErrMsg = when (state.user.nick.error) {
                is TextError -> when (state.user.nick.error) {
                    TextError.EMPTY -> stringResource(R.string.nick_empty_error)
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_NAME_LENGTH)
                    TextError.EXISTS -> stringResource(R.string.nick_exists_error)
                    else -> null
                }
                is NetworkError -> stringResource(R.string.nick_validation_unavailable)
                else -> null
            }
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Person,
                value = state.user.nick.value,
                onValueChange = {
                    onAction(SignUpConfirmationAction.NickChanged(it))
                },
                errorMsg = nickErrMsg,
                label = stringResource(R.string.nick_label),
                onFocus = {
                    onAction(SignUpConfirmationAction.NickFocused)
                },
                onTypingEnded = {
                    onAction(SignUpConfirmationAction.NickBlurred)
                }
            )
            PrimaryProgressButton(
                inProgress = state.isLoading,
                onClick = {
                    onAction(SignUpConfirmationAction.Submit)
                },
                text = stringResource(R.string.sign_up_main_button)
            )
        }
    }
}