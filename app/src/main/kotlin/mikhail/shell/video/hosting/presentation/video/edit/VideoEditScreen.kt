package mikhail.shell.video.hosting.presentation.video.edit

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.domain.errors.VideoEditingError
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.uriToFile
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun VideoEditScreen(
    modifier: Modifier = Modifier,
    state: VideoEditScreenState,
    onRefresh: () -> Unit,
    onSubmit: (VideoEditInputState) -> Unit,
    onSuccess: (Video) -> Unit,
    onCancel: (Long) -> Unit
) {
    val activity = LocalActivity.current!!
    val windowSize = calculateWindowSizeClass(activity)
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    if (state.initialVideo != null) {
        val snackbarHostState = remember { SnackbarHostState() }
        val video = state.initialVideo
        val compoundError = state.error
        var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var title by rememberSaveable { mutableStateOf(video.title) }
        var coverAction by rememberSaveable { mutableStateOf(KEEP) }
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            topBar = {
                TopBar(
                    title = "Обновить видео",
                    onPopup = { onCancel(state.initialVideo.videoId!!) },
                    inProgress = state.isLoading,
                    onSubmit = {
                        val coverFile = coverUri?.let { context.uriToFile(it) }
                        onSubmit(
                            VideoEditInputState(
                                title = title,
                                coverAction = coverAction,
                                cover = coverFile
                            )
                        )
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(scrollState)
            ) {
                val titleErrMsg = constructInfoMessage(
                    compoundError,
                    mapOf(
                        VideoEditingError.TITLE_EMPTY to "Заполните название",
                        VideoEditingError.TITLE_TOO_LARGE to "Максимальная длина ${ValidationRules.MAX_TITLE_LENGTH}"
                    )
                )
                StandardEditField(
                    firstTime = false,
                    updated = title != state.initialVideo.title,
                    empty = title.isEmpty(),
                    onDelete = { title = "" },
                    onRevert = { title = state.initialVideo.title }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        value = title,
                        onValueChange = { title = it },
                        errorMsg = titleErrMsg,
                        placeholder = "Название",
                        icon = Icons.Rounded.Title
                    )
                }
                val coverPicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
                ) {
                    if (it != null) {
                        coverUri = it
                        coverAction = UPDATE
                    }
                }
                var coverExists by rememberSaveable { mutableStateOf<Boolean?>(null) }
                val coverErrMsg = constructInfoMessage(
                    compoundError,
                    mapOf(
                        VideoEditingError.COVER_NOT_FOUND to "Обложка не найдена",
                        VideoEditingError.COVER_TYPE_NOT_VALID to "Некорректный формат изображения",
                        VideoEditingError.COVER_TOO_LARGE to "Изображение не может быть больше 10 МБ."
                    )
                )
                Column {
                    StandardEditField(
                        firstTime = false,
                        updated = coverAction == UPDATE || coverAction == REMOVE && coverExists == true,
                        empty = !(coverUri != null || coverExists == true && coverAction != REMOVE),
                        onRevert = {
                            coverUri = null
                            coverAction = KEEP
                        },
                        onDelete = {
                            coverUri = null
                            coverAction = REMOVE
                        }
                    ) {
                        FileInputField(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                coverPicker.launch("image/*")
                            },
                            placeholder = if (coverUri != null || coverExists == true && coverAction == KEEP)
                                "Изменить обложку" else "Выбрать обложку",
                            icon = Icons.Rounded.Wallpaper,
                            errorMsg = coverErrMsg
                        )
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally),
                    ) {
                        if (coverExists != false) {
                            Column (
                                modifier = Modifier.then(
                                    if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                                        Modifier.fillMaxWidth()
                                    } else {
                                        Modifier.width(350.dp)
                                    }
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Текущая обложка"
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                    model = video.coverUrl,
                                    contentDescription = video.title,
                                    onSuccess = { coverExists = true },
                                    onError = { coverExists = false }
                                )
                            }
                        }
                        if (coverUri != null) {
                            Column (
                                modifier = Modifier.then(
                                    if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                                        Modifier.fillMaxWidth()
                                    } else {
                                        Modifier.width(350.dp)
                                    }
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Выбранная обложка"
                                )
                                val painter = rememberAsyncImagePainter(model = coverUri)
                                Image(
                                    painter = painter,
                                    contentDescription = video.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    if (coverExists == true && coverAction == REMOVE) {
                        Column (
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Вы удалите обложку"
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(state.updatedVideo) {
            if (state.updatedVideo != null) {
                snackbarHostState.showSnackbar(
                    message = "Видео успешно отредактировано.",
                    duration = SnackbarDuration.Long
                )
                onSuccess(state.updatedVideo)
            }
        }
    } else if (state.isLoading) {
        LoadingComponent(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
    } else if (state.error != null) {
        ErrorComponent(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            onRetry = onRefresh
        )
    }
}

@Composable
@Preview
fun EditVideoScreenPreview() {
    VideoHostingTheme {
        VideoEditScreen(
            state = VideoEditScreenState(
                initialVideo = Video(
                    100500L,
                    1981L,
                    "Some video"
                )
            ),
            onSubmit = {},
            onSuccess = {},
            onRefresh = {},
            onCancel = {}
        )
    }
}