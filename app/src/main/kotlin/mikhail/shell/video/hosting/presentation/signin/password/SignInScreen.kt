package mikhail.shell.video.hosting.presentation.signin.password

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import mikhail.shell.video.hosting.domain.errors.SignInError
import mikhail.shell.video.hosting.domain.errors.contains

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    state: SignInWithPasswordState,
    onSubmit: (String, String) -> Unit,
    onSuccess: () -> Unit,
    onSigningUp: () -> Unit
) {
    val scrollState = rememberScrollState()
    val error = state.error
    LaunchedEffect(state.authModel) {
        if (state.authModel != null) {
            onSuccess()
        }
    }
    if (state.authModel != null) {
        Text(
            text = "Вы успешно вошли"
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        var email by remember { mutableStateOf(DEFAULT_EMAIL) }
        if (error.contains(SignInError.EMAIL_EMPTY)) {
            Text(text = "Заполните email")
        } else if (error.contains(SignInError.EMAIL_INVALID)) {
            Text(text = "Email не корректно введён")
        } else if (error.contains(SignInError.EMAIL_NOT_FOUND)) {
            Text(text = "Пользователь с email $email не найден")
        }
        TextField(
            value = email,
            onValueChange = {
                email = it
            }
        )
        var password by remember { mutableStateOf(DEFAULT_PASSWORD) }
        if (error.contains(SignInError.PASSWORD_EMPTY)) {
            Text(text = "Введите пароль")
        } else if (error.contains(SignInError.PASSWORD_INCORRECT)) {
            Text(text = "Неправильный пароль")
        }
        TextField(
            value = password,
            onValueChange = {
                password = it
            },
            visualTransformation = PasswordVisualTransformation()
        )
        Button(
            onClick = {
                onSubmit(email, password)
            }
        ) {
            Text(
                text = "Войти"
            )
        }
        Text(
            text = "Зарегистрироваться",
            modifier = Modifier.clickable {
                onSigningUp()
            }
        )

    }
}

const val DEFAULT_EMAIL = "mikhail.shell@yandex.ru"
const val DEFAULT_PASSWORD = "qwerty"