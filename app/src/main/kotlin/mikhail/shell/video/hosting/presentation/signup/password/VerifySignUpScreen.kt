package mikhail.shell.video.hosting.presentation.signup.password

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.presentation.utils.CodeInputField
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun VerifySignUpScreen(
    state: VerifySignUpScreenState,
    onEvent: (VerifySignUpUiEvent) -> Unit
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
        if (state is VerifySignUpScreenState.Entering) {
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
                val code = state.code
                val codeErrorMsg = when (state.codeError) {
                    TextError.NOT_CORRECT -> stringResource(R.string.code_not_correct)
                    else -> null
                }
                CodeInputField(
                    isValid = when {
                        state.codeError in arrayOf(
                            TextError.NOT_CORRECT, TextError.NOT_VALID
                        ) -> false
                        else -> null
                    },
                    length = ValidationRules.CODE_LENGTH,
                    onValueChange = {
                        onEvent(VerifySignUpUiEvent.CodeChanged(it))
                    }
                )
                if (codeErrorMsg != null) {
                    Text(
                        text = codeErrorMsg
                    )
                }
                LaunchedEffect(code) {
                    if (code.length == ValidationRules.CODE_LENGTH) {
                        onEvent(VerifySignUpUiEvent.Submit)
                    }
                }
            }
        } else if (state is VerifySignUpScreenState.Expired) {
            StandardComplexErrorHandler(
                error = TextError.NOT_VALID,
                snackBarHostState = snackBarHostState
            )
        }
    }

}