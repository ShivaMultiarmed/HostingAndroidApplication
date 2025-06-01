package mikhail.shell.video.hosting.presentation.channel.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.DensityMedium
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.TITLE_EMPTY
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.presentation.utils.DeletingItem
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.uriToFile
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@OptIn(UnstableApi::class)
@Composable
fun CreateChannelScreen(
    modifier: Modifier = Modifier,
    state: CreateChannelScreenState,
    onSubmit: (CreateChannelInputState) -> Unit,
    onSuccess: (Channel) -> Unit,
    onPopup: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var title by rememberSaveable { mutableStateOf("") }
    var alias by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var description by rememberSaveable { mutableStateOf("") }
    var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    Scaffold (
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = "Создать канал",
                onPopup = onPopup,
                inProgress = state.isLoading,
                onSubmit = {
                    val coverFile = coverUri?.let { context.uriToFile(it) }
                    val avatarFile = avatarUri?.let { context.uriToFile(it) }
                    val input = CreateChannelInputState(title, alias, description, coverFile, avatarFile)
                    onSubmit(input)
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
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
                    snackbarHostState.showSnackbar(message = "Канал был успешно создан", duration = SnackbarDuration.Long)
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
            if (avatarUri != null) {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Выбранный аватар")
                    val painter = rememberAsyncImagePainter(model = avatarUri)
                    Image(
                        painter = painter,
                        contentDescription = title,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
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
            if (coverUri != null) {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Выбранная обложка"
                    )
                    val painter = rememberAsyncImagePainter(model = coverUri)
                    Image(
                        painter = painter,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun CreateChannelScreenPreview() {
    VideoHostingTheme {
        CreateChannelScreen(
            state = CreateChannelScreenState(),
            onPopup = {},
            onSubmit = {},
            onSuccess = {}
        )
    }
}