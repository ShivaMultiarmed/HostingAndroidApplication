package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

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
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        var focused by rememberSaveable { mutableStateOf(false) }
        TextField(
            modifier = modifier.onFocusChanged {
                focused = it.isFocused
            },
            keyboardOptions = if (secure) KeyboardOptions(keyboardType = KeyboardType.Password) else keyboardOptions,
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
                        modifier = Modifier,
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = placeholder,
                            modifier = Modifier.size(22.dp),
                            tint = when {
                                errorMsg != null -> MaterialTheme.colorScheme.error
                                focused -> MaterialTheme.colorScheme.primary
                                !focused -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                    }
                }
            },
            shape = RoundedCornerShape(0.dp),
            colors = TextFieldDefaults.colors(
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                unfocusedIndicatorColor = Color.Transparent,
                errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                errorLabelColor = MaterialTheme.colorScheme.error,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
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
            modifier = modifier
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
fun StandardEditField(
    modifier: Modifier = Modifier,
    firstTime: Boolean = true,
    updated: Boolean = true,
    empty: Boolean,
    onDelete: () -> Unit,
    onRevert: () -> Unit,
    field: @Composable () -> Unit
) {
    val actionList = mutableListOf<ActionItem>()
    if (!firstTime && updated) {
        actionList.add(
            RevertingItem(
                reverting = onRevert
            )
        )
    }
    if (!empty) actionList.add(
        DeletingItem(
            deleting = onDelete
        )
    )
    EditField(
        modifier = modifier,
        actionItems = actionList,
        field = field
    )
}

@Composable
@Preview
fun EditInputFieldPreview() {
    VideoHostingTheme {
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
}

@Composable
@Preview
fun EditFileFieldPreview() {
    VideoHostingTheme {
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
}

open class ActionItem(
    val icon: ImageVector,
    val action: () -> Unit
)

data class DeletingItem(val deleting: () -> Unit) : ActionItem(
    icon = Icons.Rounded.Delete,
    action = deleting
)

data class RevertingItem(val reverting: () -> Unit) : ActionItem(
    icon = Icons.Rounded.Refresh,
    action = reverting
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
    VideoHostingTheme {
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
}