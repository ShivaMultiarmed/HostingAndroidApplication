package mikhail.shell.video.hosting.presentation.video.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DensitySmall
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.utils.isBlank
import mikhail.shell.video.hosting.domain.utils.isNotBlank
import mikhail.shell.video.hosting.presentation.utils.DeletingItem
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.FormMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.RemoveButton
import mikhail.shell.video.hosting.presentation.utils.RevertButton
import mikhail.shell.video.hosting.presentation.utils.RevertingItem
import mikhail.shell.video.hosting.presentation.utils.SecondaryButton
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.getFileBytes
import mikhail.shell.video.hosting.presentation.utils.uriToFile
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@Composable
fun VideoEditScreen(
    modifier: Modifier = Modifier,
    state: VideoEditScreenState,
    onRefresh: () -> Unit,
    onSubmit: (VideoEditInputState) -> Unit,
    onSuccess: (Video) -> Unit,
    onCancel: (Long) -> Unit
) {
    val scrollState = rememberScrollState()
    val contentResolver = LocalContext.current.contentResolver
    if (state.initialVideo != null) {
        val video = state.initialVideo
        val compoundError = state.error
        var coverUri by remember { mutableStateOf<Uri?>(null) }
        var title by remember { mutableStateOf(video.title) }
        var coverAction by remember { mutableStateOf(KEEP) }
        var description by remember { mutableStateOf("") }
        Scaffold (
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    topBarTitle = if (state.updatedVideo == null) "Обновить видео" else "Готово",
                    onPopup = {
                        onCancel(state.initialVideo.videoId!!)
                    },
                    buttonTitle = "Сохранить",
                    onSubmit = {
                        val coverFile = coverUri?.let { contentResolver.uriToFile(it) }
                        onSubmit(
                            VideoEditInputState(
                                title = title,
                                coverAction = coverAction,
                                cover = coverFile
                            )
                        )
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(scrollState)
            ) {
                val titleErrMsg = if (compoundError.equivalentTo(UploadVideoError.TITLE_EMPTY)) {
                    "Заполните название"
                } else null
                StandardEditField (
                    firstTime = false,
                    updated = title != state.initialVideo.title,
                    empty = title.isEmpty(),
                    onDelete = { title = "" },
                    onRevert = { title = state.initialVideo.title }
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
                StandardEditField(
                    firstTime = false,
                    updated = description != "", // TODO state.initialVideo.description
                    empty = description.isEmpty(),
                    onRevert = { description = "" },
                    onDelete = { description = "" }
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
                        icon = Icons.Rounded.DensitySmall
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
                var coverExists by remember { mutableStateOf<Boolean?>(null) }
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
                            icon = Icons.Rounded.Wallpaper
                        )
                    }
                    Column {
                        if (coverExists != false) {
                            Text(
                                text = "Текущая обложка"
                            )
                            AsyncImage(
                                modifier = Modifier.width(300.dp)
                                    .aspectRatio(16f / 9)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                                model = video.coverUrl,
                                contentDescription = video.title,
                                onSuccess = {
                                    coverExists = true
                                },
                                onError = {
                                    coverExists = false
                                }
                            )
                        }
                        if (coverUri != null) {
                            Text("Вы выбрали")
                            val painter = rememberAsyncImagePainter(model = coverUri)
                            Image(
                                painter = painter,
                                contentDescription = video.title,
                                modifier = Modifier.width(300.dp)
                                    .aspectRatio(16f / 9)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (coverExists == true) {
                            if (coverAction == REMOVE) {
                                Text(
                                    text = "Вы удалите обложку"
                                )
                            }
                        }
                    }
                }
            }
        }
        LaunchedEffect(state.updatedVideo) {
            if (state.updatedVideo != null) {
                onSuccess(state.updatedVideo)
            }
        }
    } else if (state.isLoading) {
        LoadingComponent(
            modifier = modifier.fillMaxSize()
        )
    } else if (state.error != null) {
        ErrorComponent(
            modifier = modifier.fillMaxSize(),
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