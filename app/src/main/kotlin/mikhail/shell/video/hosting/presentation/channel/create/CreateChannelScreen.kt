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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.AVATAR_NOT_FOUND
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.AVATAR_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.AVATAR_TYPE_NOT_VALID
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.COVER_NOT_FOUND
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.COVER_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.COVER_TYPE_NOT_VALID
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.DESCRIPTION_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.TITLE_EMPTY
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.TITLE_EXISTS
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError.TITLE_TOO_LARGE
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
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
                title = stringResource(R.string.channel_create_title),
                onPopup = onPopup,
                inProgress = state.isLoading,
                complete = state.channel != null,
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
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.channel_create_success),
                        duration = SnackbarDuration.Long
                    )
                    onSuccess(state.channel)
                }
            }
            val titleErrMsg = constructInfoMessage(
                state.error,
                mapOf(
                    TITLE_EMPTY to stringResource(R.string.text_empty_error),
                    TITLE_EXISTS to stringResource(R.string.channel_title_exists_error),
                    TITLE_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_TITLE_LENGTH)
                )
            )
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
                    placeholder = stringResource(R.string.channel_title_label),
                    errorMsg = titleErrMsg
                )
            }
            val aliasErrMsg = constructInfoMessage(
                state.error,
                mapOf(
                    ChannelCreationError.ALIAS_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_TITLE_LENGTH),
                    ChannelCreationError.ALIAS_EXISTS to stringResource(R.string.channel_alias_exists_error)
                )
            )
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
                    placeholder = stringResource(R.string.channel_alias_label),
                    errorMsg = aliasErrMsg
                )
            }
            val descriptionErrMsg = constructInfoMessage(
                state.error,
                mapOf(
                    DESCRIPTION_TOO_LARGE to stringResource(R.string.text_too_large_error, MAX_TEXT_LENGTH)
                )
            )
            EditField (
                actionItems = if (description.isNotEmpty()) listOf(
                    DeletingItem (
                        deleting = { description = "" }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    icon = Icons.Rounded.DensityMedium,
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    placeholder = stringResource(R.string.channel_description_label),
                    maxLines = 50,
                    errorMsg = descriptionErrMsg
                )
            }
            val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null)
                    avatarUri = it
            }
            val avatarErrMsg = constructInfoMessage(
                state.error,
                mapOf(
                    AVATAR_TOO_LARGE to stringResource(R.string.file_too_large_error, (ValidationRules.MAX_IMAGE_SIZE / 1024 / 1024).toString() + " MB"),
                    AVATAR_TYPE_NOT_VALID to stringResource(R.string.type_not_valid_error),
                    AVATAR_NOT_FOUND to stringResource(R.string.file_not_found_error)
                )
            )
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
                    placeholder = if (avatarUri == null) stringResource(R.string.channel_avatar_choose_label)
                    else stringResource(R.string.channel_avatar_choose_another_label),
                    onClick = {
                        avatarPicker.launch("image/*")
                    },
                    errorMsg = avatarErrMsg
                )
            }
            if (avatarUri != null) {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.channel_chosen_avatar_message)
                    )
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
                if (it != null) {
                    coverUri = it
                }
            }
            val coverErrMsg = constructInfoMessage(
                state.error,
                mapOf(
                    COVER_TOO_LARGE to stringResource(R.string.file_too_large_error, (ValidationRules.MAX_IMAGE_SIZE / 1024 / 1024).toString() + " MB"),
                    COVER_TYPE_NOT_VALID to stringResource(R.string.type_not_valid_error),
                    COVER_NOT_FOUND to stringResource(R.string.file_not_found_error)
                )
            )
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
                    placeholder = if (coverUri == null) stringResource(R.string.channel_choose_cover_label)
                    else stringResource(R.string.channel_avatar_choose_another_label),
                    onClick = {
                        coverPicker.launch("image/*")
                    },
                    errorMsg = coverErrMsg
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
                        text = stringResource(R.string.channel_chosen_cover_message)
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