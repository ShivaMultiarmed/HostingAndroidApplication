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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.CodeInputField
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun VerifySignUpScreen(
    userName: String,
    state: VerifySignUpScreenState,
    onVerify: (code: String) -> Unit,
    onSuccess: (token: String) -> Unit,
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
            var code by rememberSaveable { mutableStateOf("") }
            val codeErrorMsg = constructInfoMessage(
                error = state.error,
                errorMessages = mapOf(
                    SignUpError.CODE_NOT_CORRECT to stringResource(R.string.code_not_correct),
                    SignUpError.CODE_NOT_VALID to stringResource(R.string.code_not_valid)
                )
            )
            CodeInputField(
                isValid = state.token != null,
                length = ValidationRules.CODE_LENGTH,
                onValueChange = {
                    code = it
                }
            )
            LaunchedEffect(code) {
                if (code.length == ValidationRules.CODE_LENGTH) {
                    onVerify(code)
                }
            }
            LaunchedEffect(state.token) {
                if (state.token != null) {
                    onSuccess(state.token)
                }
            }
        }
    }
    StandardComplexErrorHandler(
        error = state.error,
        snackBarHostState = snackBarHostState
    )
}