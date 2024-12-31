package mikhail.shell.video.hosting.presentation.video.upload

import android.app.Instrumentation.ActivityResult
import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.contains
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorMessage
import mikhail.shell.video.hosting.presentation.utils.FormMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.SecondaryButton
import mikhail.shell.video.hosting.presentation.utils.Title

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
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                Title("Загрузить видео")
                if (state.video != null) {
                    FormMessage(
                        text = "Видео успешно опубликовано"
                    )
                }
                val contentResolver = LocalContext.current.contentResolver
                var title by remember { mutableStateOf("") }
                val compoundError = state.error
                val titleErrMsg = if (compoundError.contains(UploadVideoError.TITLE_EMPTY)) {
                    "Заполните название"
                } else null
                InputField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    errorMsg = titleErrMsg,
                    placeholder = "Название"
                )
                var channelId by remember { mutableStateOf<Long?>(null) }
                val channelErrMsg =
                    if (compoundError.contains(UploadVideoError.CHANNEL_NOT_CHOSEN)) {
                        "Выберите канал"
                    } else null
                Dropdown(
                    placeHolder = "Канал",
                    values = state.channels.associate { it.channelId!! to it.title },
                    onValueChange = {
                        channelId = it
                    },
                    errorMsg = channelErrMsg
                )
                var description by remember { mutableStateOf("") }
                InputField(
                    modifier = Modifier.height(300.dp),
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    placeholder = "Описание"
                )
                var sourceUri by remember { mutableStateOf<Uri?>(null) }
                val sourcePicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
                ) {
                    if (it != null)
                        sourceUri = it
                }
                val sourceErrMsg = if (compoundError.contains(UploadVideoError.SOURCE_EMPTY)) {
                    "Выберите видео"
                } else if (compoundError.contains(UploadVideoError.SOURCE_TYPE_INVALID)) {
                    "Некорректный формат видео"
                } else null
                if (sourceErrMsg != null) {
                    ErrorMessage(sourceErrMsg)
                }
                Row {
                    SecondaryButton(
                        onClick = {
                            sourcePicker.launch("video/*")
                        },
                        text = if (sourceUri == null) "Выбрать запись" else "Изменить запись"
                    )
                    if (sourceUri != null) {
                        SecondaryButton(
                            text = "Удалить запись",
                            onClick = {
                                sourceUri = null
                            }
                        )
                    }
                }
                var coverUri by remember { mutableStateOf<Uri?>(null) }
                val coverPicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
                ) {
                    if (it != null)
                        coverUri = it
                }
                Row {
                    SecondaryButton(
                        onClick = {
                            coverPicker.launch("image/*")
                        },
                        text = if (coverUri == null) "Выбрать обложку" else "Изменить обложку"
                    )
                    if (coverUri != null) {
                        SecondaryButton(
                            text = "Удалить обложку",
                            onClick = {
                                coverUri = null
                            }
                        )
                    }
                }
                PrimaryButton(
                    text = "Выложить",
                    onClick = {
                        val sourceFile: File?
                        if (sourceUri == null)
                            sourceFile = null
                        else {
                            val mimeType = contentResolver.getType(sourceUri!!)
                            val extension =
                                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                            sourceFile = File(
                                name = sourceUri!!.lastPathSegment + "." + extension,
                                mimeType = mimeType,
                                content = contentResolver.getFileBytes(sourceUri!!)
                            )
                        }
                        val coverFile: File?
                        if (coverUri == null)
                            coverFile = null
                        else {
                            val mimeType = contentResolver.getType(coverUri!!)
                            val extension =
                                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                            coverFile = File(
                                name = coverUri!!.lastPathSegment + "." + extension,
                                mimeType = contentResolver.getType(coverUri!!),
                                content = contentResolver.getFileBytes(coverUri!!),
                            )
                        }
                        val input = UploadVideoInput(
                            channelId = channelId,
                            title = title,
                            description = description,
                            source = sourceFile,
                            cover = coverFile,
                        )
                        onSubmit(input)
                    }
                )
            }
        }
        LaunchedEffect(state.video) {
            if (state.video != null) {
                onSuccess(state.video)
            }
        }
    } else if (state.error.contains(ChannelLoadingError.UNEXPECTED)) {
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
    onValueChange: (T) -> Unit,
    errorMsg: String? = null
) {
    var selected by remember { mutableStateOf<T?>(null) }
    var expanded by remember { mutableStateOf(false) }
    Column {
        if (errorMsg != null) {
            Text(
                text = errorMsg
            )
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = it
            }
        ) {
            InputField(
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
}

fun ContentResolver.getFileBytes(uri: Uri): ByteArray? {
    val inputStream = this.openInputStream(uri)
    return inputStream?.readBytes()
}