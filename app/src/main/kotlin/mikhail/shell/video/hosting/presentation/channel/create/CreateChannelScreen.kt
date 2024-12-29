package mikhail.shell.video.hosting.presentation.channel.create

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.TITLE_EMPTY
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.errors.contains
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.presentation.utils.FormMessage
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.SecondaryButton
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.video.upload.getFileBytes

@OptIn(UnstableApi::class)
@Composable
fun CreateChannelScreen(
    modifier: Modifier = Modifier,
    state: CreateChannelScreenState,
    onSubmit: (ChannelInputState) -> Unit,
    onSuccess: (Channel) -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
        ) {
            Title("Создать канал")
            if (state.channel != null) {
                FormMessage(
                    text = "Вы успешно создали канал"
                )
            }
            LaunchedEffect(state.channel) {
                if (state.channel != null) {
                    onSuccess(state.channel)
                }
            }
            var title by rememberSaveable { mutableStateOf("") }
            val titleErrMsg = if (state.error.contains(TITLE_EMPTY)) {
                "Заполните название"
            } else null
            InputField(
                value = title,
                onValueChange = {
                    title = it
                },
                placeholder = "Название",
                errorMsg = titleErrMsg
            )
            var alias by rememberSaveable { mutableStateOf("") }
            InputField(
                value = alias,
                onValueChange = {
                    alias = it
                },
            )
            var description by rememberSaveable { mutableStateOf("") }
            InputField(
                value = description,
                onValueChange = {
                    description = it
                },
                placeholder = "Описание",
                maxLines = 50
            )
            var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
            val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null)
                    avatarUri = it
            }
            Row {
                SecondaryButton(
                    onClick = {
                        avatarPicker.launch("image/*")
                    },
                    text = if (avatarUri == null) "Выбрать аватар" else "Поменять аватар"
                )
                if (avatarUri != null) {
                    SecondaryButton(
                        text = "Удалить аватар",
                        onClick = {
                            avatarUri = null
                        }
                    )
                }
            }
            var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
            val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null)
                    coverUri = it
            }
            Row{
                SecondaryButton(
                    onClick = {
                        coverPicker.launch("image/*")
                    },
                    text = if (coverUri == null) "Выбрать обложку" else "Поменять обложку"
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
            val contentResolver = LocalContext.current.contentResolver
            PrimaryButton(
                text = "Создать",
                onClick = {
                    val coverFile = coverUri?.let {
                        val mimeType = contentResolver.getType(it)
                        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                        File(
                            name = it.lastPathSegment+ "." + extension,
                            mimeType = mimeType,
                            content = contentResolver.getFileBytes(it)
                        )
                    }
                    val avatarFile = avatarUri?.let {
                        val mimeType = contentResolver.getType(it)
                        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                        File(
                            name = it.lastPathSegment + "." + extension,
                            mimeType = mimeType,
                            content = contentResolver.getFileBytes(it)
                        )
                    }
                    val input = ChannelInputState(title, alias, description, coverFile, avatarFile)
                    onSubmit(input)
                }
            )
        }
    }
}