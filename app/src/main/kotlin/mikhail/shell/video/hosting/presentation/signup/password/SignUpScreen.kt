package mikhail.shell.video.hosting.presentation.signup.password

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mikhail.shell.video.hosting.domain.errors.SignUpError
import mikhail.shell.video.hosting.presentation.signin.password.SignUpInputState
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.FormMessage
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
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        Title("Зарегистрироваться")
        if (state.authModel != null) {
            FormMessage(text = "Вы успешно зарегистрировались")

        }
        LaunchedEffect(state.authModel) {
            if (state.authModel != null) {
                delay(1000)
                onSuccess(state.authModel)
            }
        }
        val compoundError = state.error
        var userName by rememberSaveable { mutableStateOf("") }
        val userNameErrorMsg = if (compoundError.equivalentTo(SignUpError.EMAIL_EMPTY)) {
            "Введите почту"
        } else if (compoundError.equivalentTo(SignUpError.EMAIL_INVALID)) {
            "Некорректная почта"
        } else null
        InputField(
            modifier = Modifier.width(280.dp).clip(RoundedCornerShape(10.dp)),
            icon = Icons.Rounded.Email,
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
            placeholder = "Пароль"
        )
        var name by rememberSaveable { mutableStateOf("") }
        val nameErrMsg = if (compoundError.equivalentTo(SignUpError.NAME_EMPTY))
            "Введите имя"
        else null
        InputField(
            modifier = Modifier.width(280.dp).clip(RoundedCornerShape(10.dp)),
            icon = Icons.Rounded.Person,
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