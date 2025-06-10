package mikhail.shell.video.hosting.presentation.signup.password

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    state: SignUpWithPasswordState,
    onSubmit: (SignUpInputState) -> Unit,
    onSuccess: (AuthModel) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold (
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            Title(
                text = stringResource(R.string.sign_up_title)
            )
            LaunchedEffect(state.authModel) {
                if (state.authModel != null) {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.sign_up_success),
                        duration = SnackbarDuration.Short
                    )
                    onSuccess(state.authModel)
                }
            }
            val compoundError = state.error
            var userName by rememberSaveable { mutableStateOf("") }
            val userNameErrorMsg = constructInfoMessage(
                state.error,
                mapOf(
                    SignUpError.USERNAME_EMPTY to stringResource(R.string.email_empty_error),
                    SignUpError.USERNAME_MALFORMED to stringResource(R.string.email_malformed_error),
                    SignUpError.USERNAME_EXISTS to stringResource(R.string.email_exists_msg_error),
                    SignUpError.USERNAME_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_USERNAME_LENGTH)
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
            val passwordErrorMsg = constructInfoMessage(
                state.error,
                mapOf(
                    SignUpError.PASSWORD_EMPTY to stringResource(R.string.password_empty_error),
                    SignUpError.PASSWORD_NOT_VALID to stringResource(R.string.password_not_valid_error,
                        ValidationRules.MIN_PASSWORD_LENGTH, ValidationRules.MAX_PASSWORD_LENGTH)
                )
            )
            var password by rememberSaveable { mutableStateOf("") }
            InputField(
                modifier = Modifier.width(280.dp).clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Password,
                value = password,
                onValueChange = {
                    password = it
                },
                errorMsg = passwordErrorMsg,
                secure = true,
                placeholder = stringResource(R.string.password_label)
            )
            var nick by rememberSaveable { mutableStateOf("") }
            val nickErrMsg = constructInfoMessage(
                compoundError,
                mapOf(
                    SignUpError.NICK_EMPTY to stringResource(R.string.nick_empty_error),
                    SignUpError.NICK_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_NAME_LENGTH),
                    SignUpError.NICK_EXISTS to stringResource(R.string.nick_exists_error)
                )
            )
            InputField(
                modifier = Modifier.width(280.dp).clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Person,
                value = nick,
                onValueChange = {
                    nick = it
                },
                errorMsg = nickErrMsg,
                placeholder = stringResource(R.string.nick_label)
            )
            PrimaryButton(
                text = stringResource(R.string.sign_up_main_button),
                onClick = {
                    onSubmit(
                        SignUpInputState(
                            userName = userName,
                            password = password,
                            nick = nick
                        )
                    )
                }
            )
        }
    }
}

@Composable
@Preview
fun SignUpScreenDayPreview() {
    VideoHostingTheme {
        SignUpScreen(
            state = SignUpWithPasswordState(),
            onSubmit = {},
            onSuccess = {}
        )
    }
}
@Preview(
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun SignUpScreenNightPreview() {
    VideoHostingTheme {
        SignUpScreen(
            state = SignUpWithPasswordState(),
            onSubmit = {},
            onSuccess = {}
        )
    }
}