package mikhail.shell.video.hosting.presentation.signup.password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.presentation.utils.CodeInputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.SecondaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.signup.password.SignUpVerificationScreenState as ScreenState

@Composable
internal fun SignUpVerificationScreen(
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
            val codeErrorMsg = when (state.code.error) {
                TextError.NOT_CORRECT -> stringResource(R.string.code_not_correct)
                TextError.PATTERN -> stringResource(R.string.code_pattern_not_correct)
                TextError.NOT_VALID -> stringResource(R.string.code_not_valid)
                UnexpectedError -> stringResource(R.string.unexpected_error)
                else -> null
            }
            CodeInputField(
                isValid = state.code.error == null,
                onValueChange = {
                    onAction(ScreenAction.CodeChanged(it))
                }
            )
            if (codeErrorMsg != null) {
                Text(
                    text = codeErrorMsg
                )
            }
            Row {
                SecondaryProgressButton(
                    enabled = !state.isLoading,
                    inProgress = state.isRequestingCode,
                    text = stringResource(R.string.request_code),
                    onClick = {
                        onAction(ScreenAction.RequestCode)
                    }
                )
                PrimaryProgressButton(
                    enabled = !state.isRequestingCode,
                    inProgress = state.isLoading,
                    text = stringResource(R.string.ok_button),
                    onClick = {
                        onAction(ScreenAction.Submit)
                    }
                )
            }
        }
    }
}