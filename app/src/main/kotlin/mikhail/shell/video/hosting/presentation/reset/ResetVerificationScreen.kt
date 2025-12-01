package mikhail.shell.video.hosting.presentation.reset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.presentation.utils.CodeInputField
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun ResetVerificationScreen(
    state: ResetVerificationState,
    onAction: (ResetVerificationAction) -> Unit,
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
                text = stringResource(R.string.reset_password_title)
            )
            val codeErrorMsg = when (state.code.error) {
                TextError.NOT_CORRECT -> stringResource(R.string.code_not_correct)
                TextError.PATTERN -> stringResource(R.string.code_pattern_not_correct)
                else -> null
            }
            CodeInputField(
                isValid = state.code.error == null,
                length = ValidationRules.CODE_LENGTH,
                onValueChange = {
                    onAction(ResetVerificationAction.CodeChanged(it))
                }
            )
            if (codeErrorMsg != null) {
                Text(
                    text = codeErrorMsg
                )
            }
        }
    }

}
