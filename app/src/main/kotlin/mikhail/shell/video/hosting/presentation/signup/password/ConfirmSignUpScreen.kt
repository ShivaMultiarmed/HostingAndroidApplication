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
import mikhail.shell.video.hosting.domain.errors.authentication.SignUpError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun ConfirmSignUpScreen(
    token: String,
    state: ConfirmSignUpScreenState,
    onConfirm: (
        token: String,
        input: SignUpInputState
    ) -> Unit,
    onSuccess: (AuthModel) -> Unit
) {
    val context = LocalContext.current
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
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            Title(
                text = stringResource(R.string.sign_up_title)
            )
            LaunchedEffect(state.authModel) {
                if (state.authModel != null) {
                    snackBarHostState.showSnackbar(
                        message = context.getString(R.string.sign_up_success),
                        duration = SnackbarDuration.Short
                    )
                    onSuccess(state.authModel)
                }
            }
            var password by rememberSaveable { mutableStateOf("") }
            val passwordErrorMsg = constructInfoMessage(
                state.error,
                mapOf(
                    SignUpError.PASSWORD_EMPTY to stringResource(R.string.password_empty_error),
                    SignUpError.PASSWORD_NOT_VALID to stringResource(R.string.password_not_valid_error)
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
                    SignUpError.PASSWORDS_NOT_MATCH to stringResource(R.string.passwords_not_match)
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
            var nick by rememberSaveable { mutableStateOf("") }
            val nickErrMsg = constructInfoMessage(
                error = state.error,
                errorMessages = mapOf(
                    SignUpError.NICK_EMPTY to stringResource(R.string.nick_empty_error),
                    SignUpError.NICK_TOO_LARGE to stringResource(
                        R.string.text_too_large_error,
                        ValidationRules.MAX_NAME_LENGTH
                    ),
                    SignUpError.NICK_EXISTS to stringResource(R.string.nick_exists_error)
                )
            )
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Person,
                value = nick,
                onValueChange = {
                    nick = it
                },
                errorMsg = nickErrMsg,
                placeholder = stringResource(R.string.nick_label)
            )
            PrimaryProgressButton(
                inProgress = state.isLoading,
                complete = state.authModel != null,
                onClick = {
                    onConfirm(
                        token,
                        SignUpInputState(
                            password = password,
                            passwordDuplicate = passwordDuplicate,
                            nick = nick
                        )
                    )
                },
                text = stringResource(R.string.sign_up_main_button)
            )
        }
    }
    StandardComplexErrorHandler(
        error = state.error,
        snackBarHostState = snackBarHostState
    )
}