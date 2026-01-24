package mikhail.shell.video.hosting.presentation.channel.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_IMAGE_SIZE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.mb
import mikhail.shell.video.hosting.presentation.utils.DeletingItem
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.channel.create.ChannelCreationScreenState as ScreenState

@OptIn(UnstableApi::class)
@Composable
fun ChannelCreationScreen(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = stringResource(R.string.channel_create_title),
                onPopup = {
                    onAction(ScreenAction.Cancel)
                },
                inProgress = state.isLoading,
                onSubmit = {
                    onAction(ScreenAction.Submit)
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(scrollState)
        ) {
            val titleErrMsg = when (state.channel.title.error) {
                TextError.EMPTY -> stringResource(R.string.text_empty_error)
                TextError.LONG -> stringResource(R.string.text_too_large_error,ValidationRules.MAX_TITLE_LENGTH)
                TextError.EXISTS -> stringResource(R.string.channel_title_exists_error)
                is NetworkError -> stringResource(R.string.channel_title_check_error)
                UnexpectedError -> stringResource(R.string.unexpected_error)
                else -> null
            }
            EditField(
                actionItems = if (state.channel.title.value.isNotEmpty()) listOf(
                    DeletingItem(
                        deleting = {
                            onAction(ScreenAction.ChangeTitle(""))
                        }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Title,
                    value = state.channel.title.value,
                    onValueChange = {
                        onAction(ScreenAction.ChangeTitle(it))
                    },
                    label = stringResource(R.string.channel_title_label),
                    errorMsg = titleErrMsg,
                    onFocus = {
                        onAction(ScreenAction.FocusTitle)
                    },
                    onBlur = {
                        onAction(ScreenAction.BlurTitle)
                    }
                )
            }
            val aliasErrMsg = when (state.channel.alias.error) {
                TextError.LONG -> stringResource(R.string.text_too_large_error, ValidationRules.MAX_TITLE_LENGTH)
                TextError.EXISTS -> stringResource(R.string.channel_alias_exists_error)
                is NetworkError -> stringResource(R.string.channel_alias_check_error)
                UnexpectedError -> stringResource(R.string.unexpected_error)
                else -> null
            }
            EditField(
                actionItems = if (state.channel.alias.value.isNotEmpty()) listOf(
                    DeletingItem(
                        deleting = { onAction(ScreenAction.ChangeAlias("")) }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.AlternateEmail,
                    value = state.channel.alias.value,
                    onValueChange = {
                        onAction(ScreenAction.ChangeAlias(it))
                    },
                    label = stringResource(R.string.channel_alias_label),
                    errorMsg = aliasErrMsg,
                    onFocus = {
                        onAction(ScreenAction.FocusAlias)
                    },
                    onBlur = {
                        onAction(ScreenAction.BlurAlias)
                    }
                )
            }
            val descriptionErrMsg = when (state.channel.description.error) {
                TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TEXT_LENGTH)
                else -> null
            }
            EditField(
                actionItems = if (state.channel.description.value.isNotEmpty()) listOf(
                    DeletingItem(
                        deleting = {
                            onAction(ScreenAction.ChangeDescription(""))
                        }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    icon = Icons.Rounded.DensityMedium,
                    value = state.channel.description.value,
                    onValueChange = {
                        onAction(ScreenAction.ChangeDescription(it))
                    },
                    label = stringResource(R.string.channel_description_label),
                    maxLines = 50,
                    errorMsg = descriptionErrMsg,
                    onFocus = {
                        onAction(ScreenAction.FocusDescription)
                    },
                    onBlur = {
                        onAction(ScreenAction.BlurDescription)
                    }
                )
            }
            val logoPicker =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                    if (it != null) {
                        onAction(ScreenAction.ChangeLogo(it.toString()))
                    }
                }
            val logoErrorMsg = when (state.channel.logo.error) {
                FileError.NOT_FOUND -> stringResource(R.string.file_not_found_error)
                FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_supported)
                FileError.EMPTY -> stringResource(R.string.file_empty)
                FileError.LARGE -> stringResource(R.string.file_too_large_error,  "${MAX_IMAGE_SIZE.mb} MB")
                else -> null
            }
            EditField(
                actionItems = if (state.channel.logo.value != null) listOf(
                    DeletingItem(
                        deleting = { onAction(ScreenAction.ChangeLogo(null)) }
                    )
                ) else emptyList()
            ) {
                FileInputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Person,
                    placeholder = if (state.channel.logo.value == null) stringResource(R.string.channel_logo_choose_label)
                    else stringResource(R.string.channel_logo_choose_another_label),
                    onClick = {
                        logoPicker.launch("image/*")
                    },
                    errorMsg = logoErrorMsg
                )
            }
            if (state.channel.logo.value != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.channel_chosen_logo_message)
                    )
                    AsyncImage(
                        model = state.channel.logo.value,
                        contentDescription = state.channel.title.value,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            val headerPicker =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                    if (it != null) {
                        onAction(ScreenAction.ChangeHeader(it.toString()))
                    }
                }
            val headerErrorMsg = when (state.channel.header.error) {
                FileError.NOT_FOUND -> stringResource(R.string.file_not_found_error)
                FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_supported)
                FileError.EMPTY -> stringResource(R.string.file_empty)
                FileError.LARGE -> stringResource(R.string.file_too_large_error,  "${MAX_IMAGE_SIZE.mb} MB")
                else -> null
            }
            EditField(
                actionItems = if (state.channel.header.value != null) listOf(
                    DeletingItem(
                        deleting = { onAction(ScreenAction.ChangeHeader(null)) }
                    )
                ) else emptyList()
            ) {
                FileInputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Wallpaper,
                    placeholder = when (state.channel.header.value) {
                        null -> stringResource(R.string.channel_choose_header_label)
                        else -> stringResource(R.string.channel_header_choose_another_label)
                    },
                    onClick = {
                        headerPicker.launch("image/*")
                    },
                    errorMsg = headerErrorMsg
                )
            }
            if (state.channel.header.value != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.channel_chosen_header_message)
                    )
                    AsyncImage(
                        model = state.channel.header.value,
                        contentDescription = state.channel.title.value,
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