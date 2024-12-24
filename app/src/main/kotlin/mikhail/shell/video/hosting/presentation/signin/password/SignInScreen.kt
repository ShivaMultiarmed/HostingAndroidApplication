package mikhail.shell.video.hosting.presentation.signin.password

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import mikhail.shell.video.hosting.domain.errors.AuthError

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    state: SignInWithPasswordState,
    onSubmit: (String, String) -> Unit,
    onSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column (
        modifier = modifier.fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        var email by remember { mutableStateOf(DEFAULT_EMAIL) }
        TextField(
            value = email,
            onValueChange = {
                email = it
            }
        )
        var password by remember { mutableStateOf(DEFAULT_PASSWORD) }
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
        if (state != SignInWithPasswordState()) {
            if (state.error != null) {
                val text = when (state.error) {
                    AuthError.UNEXPECTED -> "Непредвиденная ошибка"
                }
                Text(
                    text = text
                )
            } else if (state.authModel != null) {
                Text(
                    text = "Вы успешно вошли"
                )
                onSuccess()
            }
        }

    }
}
const val DEFAULT_EMAIL = "mikhail.shell@yandex.ru"
const val DEFAULT_PASSWORD = "qwerty"