package mikhail.shell.video.hosting.presentation.signin.password

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@Composable
fun SignInScreen(
    state: SignInWithPasswordState,
    onSubmit: (String, String) -> Unit,
    onSuccess: () -> Unit,
    onSigningUp: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            val error = state.error
            Title(text = "Войти")
            LaunchedEffect(state.authModel) {
                if (state.authModel != null) {
                    snackbarHostState.showSnackbar(
                        message = "Вы успешно вошли",
                        duration = SnackbarDuration.Short
                    )
                    onSuccess()
                }
            }
            var email by rememberSaveable { mutableStateOf("") }
            val emailErrorMsg = constructInfoMessage(
                error,
                mapOf(
                    SignInError.USERNAME_EMPTY to "Заполните e-mail",
                    SignInError.USERNAME_MALFORMED to "E-mail не корректно введён",
                    SignInError.USERNAME_NOT_FOUND to "Пользователь с таким e-mail не найден"
                )
            )
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Email,
                placeholder = "E-mail",
                value = email,
                onValueChange = {
                    email = it
                },
                errorMsg = emailErrorMsg
            )
            var password by rememberSaveable { mutableStateOf("") }
            val passwordErrorMsg = constructInfoMessage(
                error,
                mapOf(
                    SignInError.PASSWORD_EMPTY to "Введите пароль",
                    SignInError.PASSWORD_INCORRECT to "Неправильный пароль"
                )
            )
            InputField(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                icon = Icons.Rounded.Password,
                placeholder = "Пароль",
                value = password,
                onValueChange = {
                    password = it
                },
                secure = true,
                errorMsg = passwordErrorMsg
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PrimaryButton(
                    text = "Войти",
                    onClick = {
                        onSubmit(email, password)
                    }
                )
                Text(
                    text = "Зарегистрироваться",
                    modifier = Modifier.clickable {
                        onSigningUp()
                    }
                )
            }
        }
    }
}
@Composable
@Preview
fun SignInScreenPreview() {
    VideoHostingTheme {
        SignInScreen(
            state = SignInWithPasswordState(),
            onSuccess = {},
            onSubmit = { a, b -> },
            onSigningUp = {}
        )
    }
}