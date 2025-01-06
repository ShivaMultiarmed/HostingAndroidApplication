package mikhail.shell.video.hosting.presentation.utils

import android.app.Notification.Action
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun InputField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit,
    errorMsg: String? = null,
    secure: Boolean = false,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Column {
        TextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            label = {
                Box(
                    modifier = Modifier
                ) {
                    Text(
                        text = placeholder
                    )
                }
            },
            leadingIcon = {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = placeholder,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(0.dp),
            colors = TextFieldDefaults.colors(
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                unfocusedIndicatorColor = Color.Transparent
            ),
            visualTransformation = if (secure) PasswordVisualTransformation() else VisualTransformation.None,
            isError = errorMsg != null,
            textStyle = TextStyle.Default.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 16.sp
            ),
            maxLines = 1,
            singleLine = maxLines == 1,
            readOnly = readOnly,
            enabled = enabled
        )
        if (errorMsg != null) {
            ErrorText(
                modifier = Modifier.padding(all = 7.dp),
                errorMsg = errorMsg
            )
        }
    }
}

@Composable
fun FileInputField(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMsg: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit = { }
) {
    Box(
        modifier = modifier
    ) {
        InputField(
            modifier = modifier,
            value = "",
            placeholder = placeholder,
            onValueChange = { },
            errorMsg = errorMsg,
            icon = icon,
            readOnly = true,
        )
        Box(
            modifier = modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun EditField(
    modifier: Modifier = Modifier,
    actionItems: List<ActionItem> = listOf(),
    field: @Composable () -> Unit
) {
    ConstraintLayout {
        val actionsBlock = createRef()
        field()
        Row(
            modifier = Modifier
                .constrainAs(actionsBlock) {
                    end.linkTo(parent.end, margin = 10.dp)
                    top.linkTo(parent.top, margin = 10.dp)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            actionItems.forEach {
                EditButton(
                    modifier = Modifier.size(34.dp),
                    imageVector = it.icon,
                    onClick = it.action
                )
            }
        }
    }
}

@Composable
@Preview
fun EditInputFieldPreview() {
    EditField(
        actionItems = listOf(
            ActionItem(
                icon = Icons.Rounded.Replay,
                action = { }
            )
        ),
    ) {
        InputFieldPreview()
    }
}

@Composable
@Preview
fun EditFileFieldPreview() {
    EditField(
        actionItems = listOf(
            ActionItem(
                icon = Icons.Rounded.Replay,
                action = { }
            )
        )
    ) {
        FileInputField(
            placeholder = "Выберите файл",
            icon = Icons.Rounded.FileUpload,
        )
    }
}


data class ActionItem(
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun ErrorText(
    modifier: Modifier = Modifier,
    errorMsg: String
) {
    Text(
        modifier = modifier,
        text = errorMsg,
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp
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
        placeholder = "Имя",
        icon = Icons.Outlined.Person,
        errorMsg = "Ошибка"
    )
}