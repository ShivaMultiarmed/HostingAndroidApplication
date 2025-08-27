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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.authentication.ResetError
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.CodeInputField
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun VerifyResetScreen(
    state: VerifyResetScreenState,
    onEvent: (VerifyResetUiEvent) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    if (state is VerifyResetScreenState.Entering) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
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
                val codeErrorMsg = constructInfoMessage(
                    error = state.error,
                    errorMessages = mapOf(
                        ResetError.CODE_NOT_VALID to stringResource(R.string.code_not_valid)
                    )
                )
                CodeInputField(
                    isValid = when {
                        state.error in arrayOf(TextError.NOT_VALID, TextError.NOT_CORRECT) -> false
                        else -> null
                    },
                    length = ValidationRules.CODE_LENGTH,
                    onValueChange = {
                        onEvent(VerifyResetUiEvent.CodeChanged(it))
                    }
                )
                if (codeErrorMsg != null) {
                    Text(
                        text = codeErrorMsg
                    )
                }
            }
        }
        StandardComplexErrorHandler(
            error = state.error,
            snackBarHostState = snackBarHostState
        )
    }
}
