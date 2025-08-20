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
import androidx.compose.material.icons.rounded.Email
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.authentication.ResetError
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun RequestResetScreen(
    state: RequestResetScreenState,
    onRequest: (userName: String) -> Unit,
    onSuccess: (userName: String) -> Unit
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
        Column (
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
            var userName by rememberSaveable { mutableStateOf("") }
            val userNameErrorMsg = constructInfoMessage(
                error = state.error,
                errorMessages = mapOf(
                    ResetError.USERNAME_EMPTY to stringResource(R.string.user_name_empty_error),
                    ResetError.USERNAME_MALFORMED to stringResource(R.string.user_name_malformed),
                    ResetError.USERNAME_NOT_FOUND to stringResource(R.string.user_name_exists)
                )
            )
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Email,
                value = userName,
                onValueChange = {
                    userName = it
                },
                errorMsg = userNameErrorMsg,
                placeholder = "E-mail"
            )
            PrimaryProgressButton(
                inProgress = state.isLoading,
                complete = state.isAccepted,
                onClick = {
                    onRequest(userName)
                },
                text = stringResource(R.string.go_forward_button)
            )
            StandardComplexErrorHandler(
                error = state.error,
                snackBarHostState = snackBarHostState
            )
            LaunchedEffect(state.isAccepted) {
                if (state.isAccepted) {
                    onSuccess(userName)
                }
            }
        }
    }
}