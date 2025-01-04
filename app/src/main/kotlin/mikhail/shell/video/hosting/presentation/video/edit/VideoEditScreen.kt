package mikhail.shell.video.hosting.presentation.video.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FormMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.RemoveButton
import mikhail.shell.video.hosting.presentation.utils.RevertButton
import mikhail.shell.video.hosting.presentation.utils.SecondaryButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.utils.getFileBytes
import mikhail.shell.video.hosting.presentation.utils.uriToFile

@Composable
fun VideoEditScreen(
    modifier: Modifier = Modifier,
    state: VideoEditScreenState,
    onRefresh: () -> Unit,
    onSubmit: (Video, EditAction, File?) -> Unit,
    onSuccess: (Video) -> Unit,
    onCancel: (Long) -> Unit
) {
    val scrollState = rememberScrollState()
    if (state.initialVideo != null) {
        val video = state.initialVideo
        val compoundError = state.error
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                Title("Редактировать видео")
                if (state.updatedVideo != null) {
                    FormMessage(
                        text = "Видео успешно отредактировано"
                    )
                }
                val contentResolver = LocalContext.current.contentResolver
                var title by remember { mutableStateOf(video.title) }
                val titleErrMsg = if (compoundError.equivalentTo(UploadVideoError.TITLE_EMPTY)) {
                    "Заполните название"
                } else null
                Row {
                    InputField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        errorMsg = titleErrMsg,
                        placeholder = "Название"
                    )
                    if (title != video.title) {
                        RevertButton(
                            onClick = {
                                title = video.title
                            }
                        )
                    }
                }
                var description by remember { mutableStateOf("") }
                Row {
                    InputField(
                        modifier = Modifier.height(300.dp),
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        placeholder = "Описание"
                    )
                }
                var coverUri by remember { mutableStateOf<Uri?>(null) }
                var coverAction by remember { mutableStateOf(KEEP) }
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
                    Row {
                        SecondaryButton(
                            onClick = {
                                coverPicker.launch("image/*")
                            },
                            text = if (coverUri != null || coverExists == true && coverAction == KEEP)
                                "Изменить обложку" else "Выбрать обложку"
                        )
                        if (coverAction == UPDATE || coverAction == REMOVE && coverExists == true) {
                            RevertButton(
                                onClick = {
                                    coverUri = null
                                    coverAction = KEEP
                                }
                            )
                        }
                        if (coverUri != null || coverExists == true && coverAction != REMOVE) {
                            RemoveButton(
                                onClick = {
                                    coverUri = null
                                    coverAction = REMOVE
                                }
                            )
                        }
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
                                contentDescription = video.title
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
                Row {
                    PrimaryButton(
                        text = "Отправить",
                        onClick = {
                            val video = state.initialVideo.copy(
                                title = title,
                            )
                            val coverFile = coverUri?.let { contentResolver.uriToFile(it) }
                            onSubmit(video, coverAction, coverFile)
                        }
                    )
                    SecondaryButton(
                        text = "Отмена",
                        onClick = {
                            onCancel(video.videoId!!)
                        }
                    )
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