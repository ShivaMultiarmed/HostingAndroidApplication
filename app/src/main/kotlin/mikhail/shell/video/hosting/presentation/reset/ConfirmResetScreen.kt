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
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.authentication.ResetError
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun ConfirmResetScreen(
    state: ConfirmResetScreenState,
    onConfirm: (password: String, passwordDuplicate: String) -> Unit,
    onSuccess: () -> Unit,
    onExpiration: () -> Unit
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
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
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            Title(
                text = stringResource(R.string.reset_password_title)
            )
            LaunchedEffect(state.isAccepted) {
                if (state.isAccepted) {
                    snackBarHostState.showSnackbar(
                        message = context.getString(R.string.reset_password_success),
                        duration = SnackbarDuration.Short
                    )
                    onSuccess()
                }
            }
            var password by rememberSaveable { mutableStateOf("") }
            val passwordErrorMsg = constructInfoMessage(
                state.error,
                mapOf(
                    ResetError.PASSWORD_EMPTY to stringResource(R.string.password_empty_error),
                    ResetError.PASSWORD_NOT_VALID to stringResource(R.string.password_not_valid_error)
                )
            )
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Password,
                value = password,
                onValueChange = {
                    password = it
                },
                errorMsg = passwordErrorMsg,
                secure = true,
                placeholder = stringResource(R.string.password_label)
            )
            var passwordDuplicate by rememberSaveable { mutableStateOf("") }
            val passwordDuplicateErrorMsg = constructInfoMessage(
                state.error,
                mapOf(
                    ResetError.PASSWORDS_NOT_MATCH to stringResource(R.string.passwords_not_match)
                )
            )
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Password,
                value = passwordDuplicate,
                onValueChange = {
                    passwordDuplicate = it
                },
                errorMsg = passwordDuplicateErrorMsg,
                secure = true,
                placeholder = stringResource(R.string.password_again_label)
            )

            PrimaryProgressButton(
                inProgress = state.isLoading,
                complete = state.isAccepted,
                onClick = {
                    onConfirm(password, passwordDuplicate)
                },
                text = stringResource(R.string.reset_password_button)
            )
        }
    }
    StandardComplexErrorHandler(
        error = state.error,
        snackBarHostState = snackBarHostState
    )
    LaunchedEffect(state.error) {
        if (state.error.equivalentTo(ResetError.TOKEN_NOT_VALID)) {
            onExpiration()
        }
    }
}