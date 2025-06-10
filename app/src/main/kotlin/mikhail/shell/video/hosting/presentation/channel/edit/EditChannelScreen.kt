package mikhail.shell.video.hosting.presentation.channel.edit

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.EditChannelError.ALIAS_EXISTS
import mikhail.shell.video.hosting.domain.errors.EditChannelError.ALIAS_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.EditChannelError.AVATAR_NOT_FOUND
import mikhail.shell.video.hosting.domain.errors.EditChannelError.AVATAR_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.EditChannelError.AVATAR_TYPE_NOT_VALID
import mikhail.shell.video.hosting.domain.errors.EditChannelError.COVER_NOT_FOUND
import mikhail.shell.video.hosting.domain.errors.EditChannelError.COVER_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.EditChannelError.COVER_TYPE_NOT_VALID
import mikhail.shell.video.hosting.domain.errors.EditChannelError.DESCRIPTION_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.EditChannelError.TITLE_EMPTY
import mikhail.shell.video.hosting.domain.errors.EditChannelError.TITLE_EXISTS
import mikhail.shell.video.hosting.domain.errors.EditChannelError.TITLE_TOO_LARGE
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.TopBar

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun EditChannelScreen(
    modifier: Modifier = Modifier,
    state: EditChannelScreenState,
    onSubmit: (EditChannelInputState) -> Unit,
    onSuccess: (Channel) -> Unit,
    onPopup: () -> Unit
) {
    val activity = LocalActivity.current!!
    val windowSize = calculateWindowSizeClass(activity)
    val snackbarHostState = remember { SnackbarHostState() }
    val initialChannel = state.initialChannel
    if (initialChannel != null) {
        var title by rememberSaveable { mutableStateOf(initialChannel.title) }
        var alias by rememberSaveable { mutableStateOf(initialChannel.alias ?: "") }
        val scrollState = rememberScrollState()
        var description by rememberSaveable { mutableStateOf(initialChannel.description ?: "") }
        var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var avatarAction by rememberSaveable { mutableStateOf(KEEP) }
        var avatarExists by rememberSaveable { mutableStateOf(null as Boolean?) }
        var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var coverAction by rememberSaveable { mutableStateOf(KEEP) }
        var coverExists by rememberSaveable { mutableStateOf(null as Boolean?) }
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            topBar = {
                TopBar(
                    title = stringResource(R.string.channel_edit_title),
                    onPopup = onPopup,
                    inProgress = state.isLoading,
                    onSubmit = {
                        val input = EditChannelInputState(
                            title,
                            alias,
                            description,
                            coverUri.toString(),
                            coverAction,
                            avatarUri.toString(),
                            avatarAction
                        )
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
                LaunchedEffect(state.editedChannel) {
                    if (state.editedChannel != null) {
                        snackbarHostState.showSnackbar(
                            message = activity.resources.getString(R.string.channel_edit_success),
                            duration = SnackbarDuration.Long
                        )
                        onSuccess(state.editedChannel)
                    }
                }
                val titleErrMsg = constructInfoMessage(
                    state.error,
                    mapOf(
                        TITLE_EMPTY to stringResource(R.string.text_empty_error),
                        TITLE_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_TITLE_LENGTH),
                        TITLE_EXISTS to stringResource(R.string.channel_title_exists_error)
                    )
                )
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = title != (initialChannel.title),
                    empty = title.isEmpty(),
                    onRevert = {
                        title = initialChannel.title
                    },
                    onDelete = {
                        title = ""
                    }
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
                        ALIAS_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_TITLE_LENGTH),
                        ALIAS_EXISTS to stringResource(R.string.channel_alias_exists_error)
                    )
                )
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = alias != (initialChannel.alias ?: ""),
                    empty = alias.isEmpty(),
                    onRevert = {
                        alias = initialChannel.alias ?: ""
                    },
                    onDelete = {
                        alias = ""
                    },
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
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = description != (initialChannel.description ?: ""),
                    empty = description.isEmpty(),
                    onRevert = {
                        description = initialChannel.description ?: ""
                    },
                    onDelete = {
                        description = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
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
                val avatarPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        if (it != null) {
                            avatarUri = it
                            avatarAction = UPDATE
                        }
                    }
                val avatarErrMsg = constructInfoMessage(
                    state.error,
                    mapOf(
                        AVATAR_TOO_LARGE to stringResource(R.string.file_too_large_error, "${ValidationRules.MAX_VIDEO_SIZE / 1024 / 1024} MB"),
                        AVATAR_TYPE_NOT_VALID to stringResource(R.string.type_not_valid_error),
                        AVATAR_NOT_FOUND to stringResource(R.string.file_not_found_error)
                    )
                )
                Column {
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = avatarAction == UPDATE || avatarAction == REMOVE && avatarExists == true,
                        empty = !(avatarUri != null || avatarExists == true && avatarAction != REMOVE),
                        onRevert = {
                            avatarUri = null
                            avatarAction = KEEP
                        },
                        onDelete = {
                            avatarUri = null
                            avatarAction = REMOVE
                        }
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
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally)
                    ) {
                        if (avatarExists != false) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.channel_current_avatar_message)
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    model = initialChannel.avatarUrl,
                                    contentDescription = title,
                                    onSuccess = {
                                        avatarExists = true
                                    },
                                    onError = {
                                        avatarExists = false
                                    }
                                )
                            }
                        }
                        if (avatarUri != null) {
                            Column (
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
                    }
                    if (avatarExists == true && avatarAction == REMOVE) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.channel_delete_avatar_message)
                            )
                        }
                    }
                }

                val coverPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        if (it != null) {
                            coverUri = it
                            coverAction = UPDATE
                        }
                    }
                val coverErrMsg = constructInfoMessage(
                    state.error,
                    mapOf(
                        COVER_TOO_LARGE to stringResource(R.string.file_too_large_error, "${ValidationRules.MAX_IMAGE_SIZE / 1024 / 1024} MB"),
                        COVER_TYPE_NOT_VALID to stringResource(R.string.type_not_valid_error),
                        COVER_NOT_FOUND to stringResource(R.string.file_not_found_error)
                    )
                )
                Column {
                    StandardEditField(
                        modifier = Modifier,
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
                            icon = Icons.Rounded.Wallpaper,
                            placeholder = if (coverUri == null) stringResource(R.string.channel_choose_cover_label)
                            else stringResource(R.string.channel_choose_another_cover_label),
                            onClick = {
                                coverPicker.launch("image/*")
                            },
                            errorMsg = coverErrMsg
                        )
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            30.dp,
                            Alignment.CenterHorizontally
                        ),
                    ) {
                        if (coverExists != false) {
                            Column(
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
                                    text = stringResource(R.string.channel_current_cover_message)
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                    model = initialChannel.coverUrl,
                                    contentDescription = title,
                                    onSuccess = { coverExists = true },
                                    onError = { coverExists = false }
                                )
                            }
                        }
                        if (coverUri != null) {
                            Column(
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
                        if (coverExists == true && coverAction == REMOVE) {
                            Text(
                                text = stringResource(R.string.channel_delete_cover_warning)
                            )
                        }
                    }
                }
            }
        }
    }
}