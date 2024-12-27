package mikhail.shell.video.hosting.presentation.signup.password

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
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import mikhail.shell.video.hosting.domain.errors.contains
import mikhail.shell.video.hosting.domain.models.AuthModel

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    state: SignUpWithPasswordState,
    onSubmit: (SignUpInputState) -> Unit,
    onSuccess: (AuthModel) -> Unit
) {
    val scrollState = rememberScrollState()
    Column (
        modifier = modifier.fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        val compoundError = state.error
        var userName by remember { mutableStateOf("") }
        TextField(
            value = userName,
            onValueChange = {
                userName = it
            }
        )
        if (compoundError.contains(SignUpError.EMAIL_EMPTY)) {
            Text("Введите почту")
        } else if (compoundError.contains(SignUpError.EMAIL_INVALID)) {
            Text("Некорректная почта")
        }
        var password by remember { mutableStateOf("") }
        TextField(
            value = password,
            onValueChange = {
                password = it
            },
            visualTransformation = PasswordVisualTransformation()
        )
        if (compoundError.contains(SignUpError.PASSWORD_EMPTY)) {
            Text("Введите пароль")
        }
        var name by remember { mutableStateOf("") }
        TextField(
            value = name,
            onValueChange = {
                name = it
            }
        )
        if (compoundError.contains(SignUpError.NAME_EMPTY)) {
            Text("Введите имя")
        }
        Button(
            onClick = {
                onSubmit(
                    SignUpInputState(
                        userName = userName,
                        password = password,
                        name = name
                    )
                )
            }
        ) {
            Text(
                text = "Зарегистрироваться"
            )
        }
        if (state.authModel != null) {
            Text(
                text = "Вы успешно зарегистрировались"
            )
            onSuccess(state.authModel)
        }
    }
}