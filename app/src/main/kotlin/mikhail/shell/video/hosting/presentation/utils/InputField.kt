package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.errors.Error

@Composable
fun InputField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit,
    errorMsg: String? = null,
    secure: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (errorMsg != null) {
            ErrorText(errorMsg)
        }
        OutlinedTextField(
            modifier = modifier.width(300.dp),
            value = value,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                errorIndicatorColor = MaterialTheme.colorScheme.error
            ),
            visualTransformation = if (secure) PasswordVisualTransformation() else VisualTransformation.None,
            isError = errorMsg != null,
            textStyle = TextStyle.Default.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            placeholder = {
                Text(text = placeholder)
            },
            singleLine = true
        )
    }
}

@Composable
fun ErrorText(errorMsg: String) {
    Text(
        text = errorMsg,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
@Preview
fun InputFieldPreview() {
    InputField(
        value = "",
        onValueChange = {

        },
        placeholder = "Имя"
        //errorMsg = "Ошибка"
    )
}