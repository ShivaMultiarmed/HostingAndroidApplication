package mikhail.shell.video.hosting.presentation.signin.password

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.presentation.utils.FormMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.domain.errors.equivalentTo

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    state: SignInWithPasswordState,
    onSubmit: (String, String) -> Unit,
    onSuccess: () -> Unit,
    onSigningUp: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        val error = state.error
        Title("Войти")
        LaunchedEffect(state.authModel) {
            if (state.authModel != null) {
                onSuccess()
            }
        }
        if (state.authModel != null) {
            FormMessage(
                text = "Вы успешно вошли"
            )
        }
        var email by remember { mutableStateOf("") }
        val emailErrorMsg = if (error.equivalentTo(SignInError.EMAIL_EMPTY)) {
            "Заполните email"
        } else if (error.equivalentTo(SignInError.EMAIL_INVALID)) {
            "Email не корректно введён"
        } else if (error.equivalentTo(SignInError.EMAIL_NOT_FOUND)) {
            "Пользователь с email $email не найден"
        } else null
        InputField(
            placeholder = "E-mail",
            value = email,
            onValueChange = {
                email = it
            },
            errorMsg = emailErrorMsg
        )
        var password by remember { mutableStateOf("") }
        val passwordErrorMsg = if (error.equivalentTo(SignInError.PASSWORD_EMPTY)) {
            "Введите пароль"
        } else if (error.equivalentTo(SignInError.PASSWORD_INCORRECT)) {
            "Неправильный пароль"
        } else null
        InputField(
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

const val DEFAULT_EMAIL = "mikhail.shell@yandex.ru"
const val DEFAULT_PASSWORD = "qwerty"


@Composable
@Preview
fun SignInScreenPreview() {
    SignInScreen(
        state = SignInWithPasswordState(),
        onSuccess = {},
        onSubmit = { a, b -> },
        onSigningUp = {}
    )
}