package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mikhail.shell.video.hosting.domain.errors.Error

@Composable
fun InputField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit,
    errorMsg: String? = null,
    secure: Boolean = false,
    maxLines: Int = 1,
    readOnly: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (errorMsg != null) {
            ErrorText(errorMsg)
        }
        BasicTextField(
            readOnly = readOnly,
            modifier = modifier.width(300.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(10.dp),
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 16.sp
            ),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = {
                it()
                if (value.isEmpty()) {
                    Text(
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = Color.Black,
                        text = placeholder
                    )
                }
            },
            maxLines = maxLines,
            visualTransformation = if (secure) PasswordVisualTransformation() else VisualTransformation.None
        )
        //OutlinedTextField(
//            shape = CircleShape,
//            colors = TextFieldDefaults.colors(
//                errorIndicatorColor = MaterialTheme.colorScheme.error,
//                unfocusedIndicatorColor = Color.Transparent
//            ),
//            visualTransformation = if (secure) PasswordVisualTransformation() else VisualTransformation.None,
//            isError = errorMsg != null,
//            textStyle = TextStyle.Default.copy(
//                color = MaterialTheme.colorScheme.onSurface,
//                fontSize = 14.sp,
//                lineHeight = 16.sp
//            ),
////            placeholder = {
////                ErrorText(errorMsg!!)
////            },
//            label = {
//                Text(
//                    text = placeholder,
//                    fontSize = 14.sp,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            },
//            singleLine = true,
        //)
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
    var value by remember { mutableStateOf("") }
    InputField(
        value = value,
        onValueChange = {
            value = it
        },
        placeholder = "Имя"
        //errorMsg = "Ошибка"
    )
}