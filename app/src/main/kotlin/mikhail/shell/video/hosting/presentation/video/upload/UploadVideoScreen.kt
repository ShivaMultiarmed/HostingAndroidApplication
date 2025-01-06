package mikhail.shell.video.hosting.presentation.video.upload

import android.app.Notification.Action
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DensityMedium
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.utils.isNotBlank
import mikhail.shell.video.hosting.presentation.utils.ActionItem
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorMessage
import mikhail.shell.video.hosting.presentation.utils.ErrorText
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.FormMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.SecondaryButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.utils.getFileBytes
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@Composable
fun UploadVideoScreen(
    modifier: Modifier = Modifier,
    state: UploadVideoScreenState,
    player: Player,
    onSubmit: (UploadVideoInput) -> Unit,
    onRefresh: () -> Unit,
    onSuccess: (Video) -> Unit
) {
    val scrollState = rememberScrollState()
    val compoundError = state.error
    if (state.channels != null) {
        var title by rememberSaveable { mutableStateOf("") }
        val contentResolver = LocalContext.current.contentResolver
        var sourceUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var channelId by rememberSaveable { mutableStateOf<Long?>(null) }
        var description by rememberSaveable { mutableStateOf("") }

        Scaffold (
            topBar = {
                Row (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Вернуться назад",
                        modifier = Modifier.clickable { 

                        }
                    )
                    Title(
                        text = if (state.video == null) "Выложить видео" else "Вы успешно выложили видео"
                    )
                    PrimaryButton(
                        modifier = Modifier.padding(start = 10.dp),
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
            },
            modifier = modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(scrollState)
                ) {
                    if (state.video != null) {
                        FormMessage(
                            text = "Видео успешно опубликовано"
                        )
                    }

                    val sourcePicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetContent()
                    ) {
                        if (it != null)
                            sourceUri = it
                    }
                    val sourceErrMsg = if (compoundError.equivalentTo(UploadVideoError.SOURCE_EMPTY)) {
                        "Выберите видео"
                    } else if (compoundError.equivalentTo(UploadVideoError.SOURCE_TYPE_INVALID)) {
                        "Некорректный формат видео"
                    } else null

                    val sourceActionItems = if (sourceUri == null) listOf()
                    else listOf(
                        ActionItem(
                            icon = Icons.Rounded.Delete,
                            action = {
                                sourceUri = null
                            }
                        )
                    )
                    Row {
                        EditField(
                            actionItems = sourceActionItems
                        ) {
                            FileInputField(
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = if (sourceUri == null) "Выбрать запись" else "Изменить запись",
                                onClick = {
                                    sourcePicker.launch("video/*")
                                },
                                icon = Icons.Rounded.VideoLibrary,
                                errorMsg = sourceErrMsg
                            )
                        }
                    }
                    if (sourceUri != null)
                        player.setMediaItem(MediaItem.fromUri(sourceUri!!))
                    else
                        player.clearMediaItems()
                    if (sourceUri != null) {
                        PlayerComponent(
                            modifier = Modifier.fillMaxWidth(),
                            player = player
                        )
                    }

                    val titleErrMsg = if (compoundError.equivalentTo(UploadVideoError.TITLE_EMPTY)) {
                        "Заполните название"
                    } else null
                    val titleActionItems = if (title.isBlank()) emptyList() else listOf(
                        ActionItem(
                            icon = Icons.Rounded.Delete,
                            action = {
                                title = ""
                            }
                        )
                    )
                    EditField (
                        actionItems = titleActionItems
                    ) {
                        InputField(
                            modifier = Modifier.fillMaxWidth(),
                            value = title,
                            onValueChange = {
                                title = it
                            },
                            errorMsg = titleErrMsg,
                            placeholder = "Название",
                            icon = Icons.Rounded.Title
                        )
                    }

                    val channelErrMsg =
                        if (compoundError.equivalentTo(UploadVideoError.CHANNEL_NOT_CHOSEN)) {
                            "Выберите канал"
                        } else null
                    val channelActionItems = if (channelId == null) emptyList() else listOf(
                        ActionItem(
                            icon = Icons.Rounded.Delete,
                            action = {
                                channelId = null
                            }
                        )
                    )
                    EditField (
                        actionItems = channelActionItems
                    ) {
                        Dropdown(
                            selected = channelId,
                            modifier = Modifier.fillMaxWidth(),
                            placeHolder = "Канал",
                            values = state.channels.associate { it.channelId!! to it.title },
                            onValueChange = {
                                channelId = it
                            },
                            errorMsg = channelErrMsg,
                            icon = Icons.Rounded.Apps
                        )
                    }


                    val coverPicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetContent()
                    ) {
                        if (it != null)
                            coverUri = it
                    }
                    EditField (
                        actionItems = if (coverUri == null) emptyList() else listOf(
                            ActionItem(
                                icon = Icons.Rounded.Delete,
                                action = {
                                    coverUri = null
                                }
                            )
                        )
                    ) {
                        FileInputField(
                            placeholder = if (coverUri == null) "Выбрать обложку" else "Изменить обложку",
                            onClick = {
                                coverPicker.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Image
                        )
                    }
                    if (coverUri != null) {
                        Image(
                            modifier = Modifier.fillMaxSize().aspectRatio(16f / 9),
                            contentScale = ContentScale.Crop,
                            painter = rememberAsyncImagePainter(coverUri),
                            contentDescription = "Обложка для видео"
                        )
                    }


                    EditField (
                        actionItems = if (description.isEmpty()) emptyList() else listOf(
                            ActionItem(
                                icon = Icons.Rounded.Delete,
                                action = {
                                    description = ""
                                }
                            )
                        )
                    ) {
                        InputField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            value = description,
                            onValueChange = {
                                description = it
                            },
                            placeholder = "Описание",
                            icon = Icons.Rounded.DensityMedium
                        )
                    }
                }
            }
        }
        LaunchedEffect(state.video) {
            if (state.video != null) {
                onSuccess(state.video)
            }
        }
    } else if (state.error.equivalentTo(ChannelLoadingError.UNEXPECTED)) {
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
    modifier: Modifier = Modifier,
    placeHolder: String,
    selected: T?,
    values: Map<T, String>,
    onValueChange: (T) -> Unit,
    errorMsg: String? = null,
    icon: ImageVector? = null
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column {
        Box {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                }
            ) {
                InputField(
                    modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    readOnly = true,
                    value = values[selected] ?: "",
                    onValueChange = {},
                    icon = icon,
                    placeholder = placeHolder,
                    errorMsg = errorMsg
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
            Box(
                modifier = modifier.matchParentSize().clickable {
                    expanded = true
                }
            )
        }
    }
}

@Composable
@Preview
fun UploadVideoScreenPreview() {
    VideoHostingTheme {
        val player = ExoPlayer.Builder(LocalContext.current)
            .build()
        UploadVideoScreen(
            state = UploadVideoScreenState(
                channels = listOf(
                    Channel(
                        channelId = 100500,
                        ownerId = 100,
                        title = "Канал №1"
                    )
                ),
            ),
            player = player,
            onSubmit = {},
            onRefresh = {},
            onSuccess = {}
        )
    }
}

