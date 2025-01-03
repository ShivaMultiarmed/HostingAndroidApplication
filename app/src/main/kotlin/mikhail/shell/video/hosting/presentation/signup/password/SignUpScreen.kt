package mikhail.shell.video.hosting.presentation.signup.password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.presentation.utils.FormMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.Title

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    state: SignUpWithPasswordState,
    onSubmit: (SignUpInputState) -> Unit,
    onSuccess: (AuthModel) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp,  Alignment.CenterVertically)
    ) {
        Title("Зарегистрироваться")
        if (state.authModel != null) {
            FormMessage(text = "Вы успешно зарегистрировались")
            onSuccess(state.authModel)
        }
        val compoundError = state.error
        var userName by remember { mutableStateOf("") }
        val userNameErrorMsg = if (compoundError.equivalentTo(SignUpError.EMAIL_EMPTY)) {
            "Введите почту"
        } else if (compoundError.equivalentTo(SignUpError.EMAIL_INVALID)) {
            "Некорректная почта"
        } else null
        InputField(
            value = userName,
            onValueChange = {
                userName = it
            },
            errorMsg = userNameErrorMsg,
            placeholder = "E-mail"
        )
        val passwordErrorMsg = if (compoundError.equivalentTo(SignUpError.PASSWORD_EMPTY)) {
            "Введите пароль"
        } else null
        var password by remember { mutableStateOf("") }
        InputField(
            value = password,
            onValueChange = {
                password = it
            },
            errorMsg = passwordErrorMsg,
            secure = true,
            placeholder = "Пароль"
        )
        var name by remember { mutableStateOf("") }
        val nameErrMsg = if (compoundError.equivalentTo(SignUpError.NAME_EMPTY))
            "Введите имя"
        else null
        InputField(
            value = name,
            onValueChange = {
                name = it
            },
             errorMsg = nameErrMsg,
            placeholder = "Имя"
        )

        PrimaryButton(
            text = "Зарегистрироваться",
            onClick = {
                onSubmit(
                    SignUpInputState(
                        userName = userName,
                        password = password,
                        name = name
                    )
                )
            }
        )
    }
}

@Composable
@Preview
fun SignUpScreenPreview() {
    SignUpScreen(
        state = SignUpWithPasswordState(),
        onSubmit = {},
        onSuccess = {}
    )
}