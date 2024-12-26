package mikhail.shell.video.hosting.presentation.video.upload

import android.app.Instrumentation.ActivityResult
import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent

@Composable
fun UploadVideoScreen(
    modifier: Modifier = Modifier,
    state: UploadVideoScreenState,
    onSubmit: (UploadVideoInput) -> Unit,
    onRefresh: () -> Unit,
    onSuccess: (Video) -> Unit
) {
    val scrollState = rememberScrollState()
    if (state.channels != null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            val contentResolver = LocalContext.current.contentResolver
            var title by remember { mutableStateOf("Some video title") } // TODO
            TextField(
                value = title,
                onValueChange = {
                    title = it
                }
            )
            var channelId by remember { mutableStateOf<Long?>(null) }
            Dropdown(
                placeHolder = "Выберите канал",
                values = state.channels.associate { it.channelId!! to it.title },
                onValueChange = {
                    channelId = it
                }
            )
            var description by remember { mutableStateOf("Some video description") } // TODO
            TextField(
                value = description,
                onValueChange = {
                    description = it
                }
            )
            var sourceUri by remember { mutableStateOf<Uri?>(null) }
            val sourcePicker = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) {
                sourceUri = it
            }
            Button(
                onClick = {
                    sourcePicker.launch("video/mp4")
                }
            ) {
                Text(
                    text = "Выбрать запись"
                )
            }
            var coverUri by remember { mutableStateOf<Uri?>(null) }
            val coverPicker = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) {
                coverUri = it
            }
            Button(
                onClick = {
                    coverPicker.launch("image/png")
                }
            ) {
                Text(
                    text = "Выбрать обложку"
                )
            }
            Button(
                onClick = {
                    val sourceFile: File?
                    if (sourceUri == null)
                        sourceFile = null
                    else
                        sourceFile = File(
                            name = sourceUri!!.lastPathSegment,
                            mimeType = contentResolver.getType(sourceUri!!),
                            content = contentResolver.getFileBytes(sourceUri!!)
                        )
                    val coverFile: File?
                    if (coverUri == null)
                        coverFile = null
                    else
                        coverFile = File(
                            name = coverUri!!.lastPathSegment,
                            mimeType = contentResolver.getType(coverUri!!),
                            content = contentResolver.getFileBytes(coverUri!!),
                        )
                    val input = UploadVideoInput(
                        channelId = channelId,
                        title = title,
                        description = description,
                        source = sourceFile,
                        cover = coverFile,
                    )
                    onSubmit(input)
                }
            ) {
                Text(
                    text = "Выложить"
                )
            }
        }
        if (state.error != null) {
            val errorMsg = when(state.error) {
                else -> "Непредвиденная ошибка"
            }
            Text(
                text = errorMsg
            )
        } else if (state.video != null) {
            Text (
                text = "Видео успешно опубликовано"
            )
            onSuccess(state.video)
        }
    } else if (state.error != null) {
        ErrorComponent(
            modifier = Modifier.fillMaxSize(),
            onRetry = {
                onRefresh()
            }
        )
    } else if (state.isLoading) {
        LoadingComponent(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dropdown(
    placeHolder: String,
    values: Map<T, String>,
    onValueChange: (T) -> Unit
) {
    var selected by remember { mutableStateOf<T?>(null) }
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
        }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = values[selected] ?: placeHolder,
            onValueChange = {},
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            values.forEach {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        selected = it.key
                        onValueChange(it.key)
                    },
                    text = {
                        Text(
                            text = it.value
                        )
                    }
                )
            }
        }
    }
}

fun ContentResolver.getFileBytes(uri: Uri): ByteArray? {
    val inputStream = this.openInputStream(uri)
    return inputStream?.readBytes()
}