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
import mikhail.shell.video.hosting.domain.errors.AuthError
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    state: SignUpWithPasswordState,
    onSubmit: (SignUpInputState) -> Unit,
    onSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column (
        modifier = modifier.fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        var userName by remember { mutableStateOf("") }
        TextField(
            value = userName,
            onValueChange = {
                userName = it
            }
        )
        var password by remember { mutableStateOf("") }
        TextField(
            value = password,
            onValueChange = {
                password = it
            },
            visualTransformation = PasswordVisualTransformation()
        )
        var name by remember { mutableStateOf("") }
        TextField(
            value = name,
            onValueChange = {
                name = it
            }
        )
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
        if (state.error != null) {
            val errorMsg = when (state.error) {
                AuthError.FIELDS_EMPTY -> "Не заполнены поля."
                AuthError.UNEXPECTED -> "Непредвиденная ошибка"
            }
            Text(
                text = errorMsg
            )
        } else if (state.authModel != null) {
            Text(
                text = "Вы успешно зарегистрировались"
            )
            onSuccess()
        }
    }
}