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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.DensityMedium
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.TITLE_EMPTY
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.presentation.utils.DeletingItem
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.SecondaryButton
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.getFileBytes

@OptIn(UnstableApi::class)
@Composable
fun CreateChannelScreen(
    modifier: Modifier = Modifier,
    state: CreateChannelScreenState,
    onSubmit: (ChannelInputState) -> Unit,
    onSuccess: (Channel) -> Unit,
    onPopup: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var alias by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var description by rememberSaveable { mutableStateOf("") }
    var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val contentResolver = LocalContext.current.contentResolver
    Scaffold (
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(
                topBarTitle = "Создать канал",
                onPopup = onPopup,
                buttonTitle = "Создать",
                onSubmit = {
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(scrollState)
        ) {
            LaunchedEffect(state.channel) {
                if (state.channel != null) {
                    onSuccess(state.channel)
                }
            }
            val titleErrMsg = if (state.error.equivalentTo(TITLE_EMPTY)) {
                "Заполните название"
            } else null
            EditField (
                actionItems = if (title.isNotEmpty()) listOf(
                    DeletingItem (
                        deleting = { title = "" }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Title,
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    placeholder = "Название",
                    errorMsg = titleErrMsg
                )
            }
            EditField (
                actionItems = if (alias.isNotEmpty()) listOf(
                    DeletingItem (
                        deleting = { alias = "" }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.AlternateEmail,
                    value = alias,
                    onValueChange = {
                        alias = it
                    },
                    placeholder = "Никнейм канала"
                )
            }
            EditField (
                actionItems = if (description.isNotEmpty()) listOf(
                    DeletingItem (
                        deleting = { description = "" }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.DensityMedium,
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    placeholder = "Описание",
                    maxLines = 50,
                )
            }
            val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null)
                    avatarUri = it
            }
            EditField (
                actionItems = if (avatarUri != null) listOf(
                    DeletingItem (
                        deleting = { avatarUri = null }
                    )
                ) else emptyList()
            ) {
                FileInputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Person,
                    placeholder = if (avatarUri == null) "Выбрать аватар канала" else "Поменять аватар канала",
                    onClick = {
                        avatarPicker.launch("image/*")
                    }
                )
            }
            val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null)
                    coverUri = it
            }

            EditField (
                actionItems = if (coverUri != null) listOf(
                    DeletingItem (
                        deleting = { coverUri = null }
                    )
                ) else emptyList()
            ) {
                FileInputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Wallpaper,
                    placeholder = if (coverUri == null) "Выбрать обложку" else "Поменять обложку",
                    onClick = {
                        coverPicker.launch("image/*")
                    }
                )
            }
        }
    }
}