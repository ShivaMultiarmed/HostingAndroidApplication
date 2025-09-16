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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_IMAGE_SIZE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.mb
import mikhail.shell.video.hosting.presentation.utils.DeletingItem
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.TopBar

@OptIn(UnstableApi::class)
@Composable
fun ChannelCreationScreen(
    state: ChannelCreationScreenState,
    onEvent: (ChannelCreationUiEvent) -> Unit
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = stringResource(R.string.channel_create_title),
                onPopup = {
                    onEvent(ChannelCreationUiEvent.Cancel)
                },
                inProgress = state.isLoading,
                complete = state.channelId != null,
                onSubmit = {
                    onEvent(ChannelCreationUiEvent.Submit)
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(scrollState)
        ) {
            LaunchedEffect(state.channelId) {
                if (state.channelId != null) {
                    snackBarHostState.showSnackbar(
                        message = context.getString(R.string.channel_create_success),
                        duration = SnackbarDuration.Long
                    )
                }
            }
            val titleErrMsg = when (state.title.error) {
                TextError.EMPTY -> stringResource(R.string.text_empty_error)
                TextError.LONG -> stringResource(R.string.text_too_large_error,ValidationRules.MAX_TITLE_LENGTH)
                TextError.EXISTS -> stringResource(R.string.channel_title_exists_error)
                is NetworkError -> stringResource(R.string.channel_title_check_error)
                else -> null
            }
            EditField(
                actionItems = if (state.title.value.isNotEmpty()) listOf(
                    DeletingItem(
                        deleting = { onEvent(ChannelCreationUiEvent.TitleChanged("")) }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Title,
                    value = state.title.value,
                    onValueChange = {
                        onEvent(ChannelCreationUiEvent.TitleChanged(it))
                    },
                    placeholder = stringResource(R.string.channel_title_label),
                    errorMsg = titleErrMsg,
                    onFocus = {
                        onEvent(ChannelCreationUiEvent.TitleFocused)
                    },
                    onBlur = {
                        onEvent(ChannelCreationUiEvent.TitleBlurred)
                    }
                )
            }
            val aliasErrMsg = when (state.alias.error) {
                TextError.LONG -> stringResource(R.string.text_too_large_error, ValidationRules.MAX_TITLE_LENGTH)
                TextError.EXISTS -> stringResource(R.string.channel_alias_exists_error)
                is NetworkError -> stringResource(R.string.channel_alias_check_error)
                else -> null
            }
            EditField(
                actionItems = if (state.alias.value.isNotEmpty()) listOf(
                    DeletingItem(
                        deleting = { onEvent(ChannelCreationUiEvent.AliasChanged("")) }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.AlternateEmail,
                    value = state.alias.value,
                    onValueChange = {
                        onEvent(ChannelCreationUiEvent.AliasChanged(it))
                    },
                    placeholder = stringResource(R.string.channel_alias_label),
                    errorMsg = aliasErrMsg,
                    onFocus = {
                        onEvent(ChannelCreationUiEvent.AliasFocused)
                    },
                    onBlur = {
                        onEvent(ChannelCreationUiEvent.AliasBlurred)
                    }
                )
            }
            val descriptionErrMsg = when (state.description.error) {
                TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TEXT_LENGTH)
                else -> null
            }
            EditField(
                actionItems = if (state.description.value.isNotEmpty()) listOf(
                    DeletingItem(
                        deleting = { onEvent(ChannelCreationUiEvent.DescriptionChanged("")) }
                    )
                ) else emptyList()
            ) {
                InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    icon = Icons.Rounded.DensityMedium,
                    value = state.description.value,
                    onValueChange = {
                        onEvent(ChannelCreationUiEvent.DescriptionChanged(it))
                    },
                    placeholder = stringResource(R.string.channel_description_label),
                    maxLines = 50,
                    errorMsg = descriptionErrMsg,
                    onFocus = {
                        onEvent(ChannelCreationUiEvent.DescriptionFocused)
                    },
                    onBlur = {
                        onEvent(ChannelCreationUiEvent.DescriptionBlurred)
                    }
                )
            }
            val logoPicker =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                    if (it != null) {
                        onEvent(ChannelCreationUiEvent.LogoChanged(it.toString()))
                    }
                }
            val logoErrorMsg = when (state.logo.error) {
                FileError.EMPTY -> stringResource(R.string.file_not_found_error)
                FileError.LARGE -> stringResource(R.string.file_too_large_error,  "${MAX_IMAGE_SIZE.mb} MB")
                FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                else -> null
            }
            EditField(
                actionItems = if (state.logo.value != null) listOf(
                    DeletingItem(
                        deleting = { onEvent(ChannelCreationUiEvent.LogoChanged(null)) }
                    )
                ) else emptyList()
            ) {
                FileInputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Person,
                    placeholder = if (state.logo.value == null) stringResource(R.string.channel_logo_choose_label)
                    else stringResource(R.string.channel_logo_choose_another_label),
                    onClick = {
                        logoPicker.launch("image/*")
                    },
                    errorMsg = logoErrorMsg
                )
            }
            if (state.logo.value != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.channel_chosen_logo_message)
                    )
                    AsyncImage(
                        model = state.logo.value,
                        contentDescription = state.title.value,
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
                        onEvent(ChannelCreationUiEvent.HeaderChanged(it.toString()))
                    }
                }
            val headerErrorMsg = when (state.header.error) {
                FileError.EMPTY -> stringResource(R.string.file_not_found_error)
                FileError.LARGE -> stringResource(R.string.file_too_large_error, "${MAX_IMAGE_SIZE.mb} MB")
                FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                else -> null
            }
            EditField(
                actionItems = if (state.header.value != null) listOf(
                    DeletingItem(
                        deleting = { onEvent(ChannelCreationUiEvent.HeaderChanged(null)) }
                    )
                ) else emptyList()
            ) {
                FileInputField(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Wallpaper,
                    placeholder = when (state.header.value) {
                        null -> stringResource(R.string.channel_choose_header_label)
                        else -> stringResource(R.string.channel_header_choose_another_label)
                    },
                    onClick = {
                        headerPicker.launch("image/*")
                    },
                    errorMsg = headerErrorMsg
                )
            }
            if (state.header.value != null) {
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
                        model = state.header.value,
                        contentDescription = state.title.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        StandardComplexErrorHandler(
            error = state.error,
            snackBarHostState = snackBarHostState
        )
    }
}