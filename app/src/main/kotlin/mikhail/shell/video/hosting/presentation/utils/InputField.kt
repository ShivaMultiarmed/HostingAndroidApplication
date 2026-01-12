package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key.Companion.Backspace
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.delay
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import kotlin.time.Duration.Companion.seconds

@Composable
fun InputField(
    modifier: Modifier = Modifier,
    label: String = "",
    value: String,
    onValueChange: (String) -> Unit,
    errorMsg: String? = null,
    isError: Boolean = errorMsg != null,
    secured: Boolean = false,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onTypingStarted: (() -> Unit)? = null,
    onTypingEnded: (() -> Unit)? = null,
    onFocus: (() -> Unit)? = null,
    onBlur: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var focusedEarlier by rememberSaveable { mutableStateOf(false) }
    var focused by rememberSaveable { mutableStateOf(false) }
    var isTyping by rememberSaveable { mutableStateOf(false) }
    var exposeText by rememberSaveable { mutableStateOf(!secured) }
    TextField(
        modifier = modifier.onFocusChanged {
            focused = it.isFocused
            if (focused) {
                onFocus?.invoke()
                if (!focusedEarlier) {
                    focusedEarlier = true
                }
            } else if (focusedEarlier) {
                onBlur?.invoke()
            }
        },
        keyboardOptions = when {
            secured -> KeyboardOptions(keyboardType = KeyboardType.Password)
            else -> keyboardOptions
        },
        value = value,
        onValueChange = {
            if (!isTyping) {
                isTyping = true
                onTypingStarted?.invoke()
            }
            onValueChange(it)
        },
        label = {
            Box {
                Text(
                    text = label
                )
            }
        },
        leadingIcon = when {
            icon != null -> {
                @Composable {
                    Box(
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
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
            }

            else -> null
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
        visualTransformation = when {
            secured && !exposeText -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },
        isError = isError,
        textStyle = TextStyle.Default.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            lineHeight = 16.sp
        ),
        maxLines = maxLines,
        singleLine = maxLines == 1,
        readOnly = readOnly,
        enabled = enabled,
        trailingIcon = {
            if (secured) {
                IconButton(
                    onClick = {
                        exposeText = !exposeText
                    }
                ) {
                    Icon(
                        contentDescription = null,
                        imageVector = when {
                            exposeText -> Icons.Rounded.Visibility
                            else -> Icons.Rounded.VisibilityOff
                        }
                    )
                }
            }
        },
        supportingText = when {
            errorMsg != null -> {
                @Composable {
                    ErrorText(
                        errorMsg = errorMsg
                    )
                }
            }
            else -> null
        }
    )
    LaunchedEffect(isTyping) {
        if (isTyping) {
            delay(2.seconds)
            isTyping = false
            onTypingEnded?.invoke()
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
            label = placeholder,
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
    edited: Boolean = true,
    empty: Boolean,
    onRemove: (() -> Unit)? = null,
    onRevert: (() -> Unit)? = null,
    field: @Composable () -> Unit
) {
    val actionList = mutableListOf<ActionItem>()
    if (!firstTime && edited && onRevert != null) {
        actionList.add(RevertingItem(reverting = onRevert))
    }
    if (!empty && onRemove != null) {
        actionList.add(DeletingItem(deleting = onRemove))
    }
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
            label = "Имя",
            icon = Icons.Outlined.Person,
            errorMsg = "Ошибка"
        )
    }
}

@Composable
fun CodeInputField(
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    isValid: Boolean? = null,
    length: Int = 4
) {
    val focusRequesters = remember { List(length) { FocusRequester() } }
    val inputStates = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<String>().apply {
            repeat(length) { add("") }
        }
    }
    val code by remember {
        derivedStateOf {
            inputStates.joinToString("")
        }
    }
    LaunchedEffect(code) {
        onValueChange(code)
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(length) { i ->
            OutlinedTextField(
                modifier = Modifier
                    .width(50.dp)
                    .focusRequester(focusRequesters[i])
                    .onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyDown) {
                            if (it.key == Backspace && inputStates[i].isEmpty() && i > 0) {
                                focusRequesters[i - 1].requestFocus()
                                inputStates[i - 1] = ""
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    },
                colors = TextFieldDefaults.colors(
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.secondary
                ),
                isError = isValid == false,
                shape = RoundedCornerShape(5.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii
                ),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                value = inputStates[i],
                onValueChange = {
                    val filteredCharacter = it.filter { ch -> ch.isLetterOrDigit() }
                    if (filteredCharacter.length <= 1) {
                        inputStates[i] = it
                        if (filteredCharacter.isNotEmpty() && i < length - 1) {
                            focusRequesters[i + 1].requestFocus()
                        }
                    }
                },
                singleLine = true
            )
        }
    }
}

@Preview
@Composable
fun CodeInputPreview() {
    var value by rememberSaveable { mutableStateOf("") }
    VideoHostingTheme {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = value,
                    color = Color.Black
                )
                CodeInputField(
                    onValueChange = {
                        value = it
                    }
                )
            }
        }
    }
}

