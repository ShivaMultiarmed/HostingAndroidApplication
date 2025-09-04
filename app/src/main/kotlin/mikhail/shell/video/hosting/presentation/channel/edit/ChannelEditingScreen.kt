package mikhail.shell.video.hosting.presentation.channel.edit

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_IMAGE_SIZE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TITLE_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_VIDEO_SIZE
import mikhail.shell.video.hosting.domain.validation.mb
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.TopBar

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ChannelEditingScreen(
    state: ChannelEditingScreenState,
    onEvent: (ChannelEditingUiEvent) -> Unit
) {
    val activity = LocalActivity.current!!
    val windowSize = calculateWindowSizeClass(activity)
    val snackBarHostState = remember { SnackbarHostState() }
    if (state is ChannelEditingScreenState.Editing) {
        val scrollState = rememberScrollState()

        val initial = state.initialChannel
        val current = state.editedChannel

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            topBar = {
                TopBar(
                    title = stringResource(R.string.channel_edit_title),
                    onPopup = {
                        onEvent(ChannelEditingUiEvent.Cancel)
                    },
                    inProgress = state.isLoading,
                    complete = !state.isLoading && state.error == null,
                    onSubmit = {
                        onEvent(ChannelEditingUiEvent.Submit)
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
                LaunchedEffect(state.error, state.isLoading) {
                    if (state.error == null && !state.isLoading) {
                        snackBarHostState.showSnackbar(
                            message = activity.resources.getString(R.string.channel_edit_success),
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                val titleErrMsg = when(current.titleError) {
                    TextError.EMPTY -> stringResource(R.string.text_empty_error)
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                    TextError.EXISTS -> stringResource(R.string.channel_title_exists_error)
                    else -> null
                }
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = current.title != (initial.title),
                    empty = current.title.isEmpty(),
                    onRevert = {
                        onEvent(ChannelEditingUiEvent.TitleChanged(initial.title))
                    },
                    onDelete = {
                        onEvent(ChannelEditingUiEvent.TitleChanged(""))
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.Title,
                        value = current.title,
                        onValueChange = {
                            onEvent(ChannelEditingUiEvent.TitleChanged(it))
                        },
                        placeholder = stringResource(R.string.channel_title_label),
                        errorMsg = titleErrMsg,
                        onTypingStarted = {
                            onEvent(ChannelEditingUiEvent.TitleTypingStarted)
                        },
                        onTypingEnded = {
                            onEvent(ChannelEditingUiEvent.TitleTypingEnded)
                        }
                    )
                }
                val aliasErrMsg = when (current.aliasError) {
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                    TextError.EXISTS -> stringResource(R.string.channel_alias_exists_error)
                    else -> null
                }
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = current.alias != (initial.alias),
                    empty = current.alias.isEmpty(),
                    onRevert = {
                        onEvent(ChannelEditingUiEvent.AliasChanged(initial.alias))
                    },
                    onDelete = {
                        onEvent(ChannelEditingUiEvent.AliasChanged(""))
                    },
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.AlternateEmail,
                        value = current.alias,
                        onValueChange = {
                            onEvent(ChannelEditingUiEvent.AliasChanged(it))
                        },
                        placeholder = stringResource(R.string.channel_alias_label),
                        errorMsg = aliasErrMsg,
                        onTypingStarted = {
                            onEvent(ChannelEditingUiEvent.AliasTypingStarted)
                        },
                        onTypingEnded = {
                            onEvent(ChannelEditingUiEvent.AliasTypingEnded)
                        }
                    )
                }
                val descriptionErrMsg = when (current.descriptionError) {
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TEXT_LENGTH)
                    else -> null
                }
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = current.description != initial.description,
                    empty = current.description.isEmpty(),
                    onRevert = {
                        onEvent(ChannelEditingUiEvent.DescriptionChanged(initial.description))
                    },
                    onDelete = {
                        onEvent(ChannelEditingUiEvent.DescriptionChanged(""))
                    }
                ) {
                    InputField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        icon = Icons.Rounded.DensityMedium,
                        value = current.description,
                        onValueChange = {
                            onEvent(ChannelEditingUiEvent.DescriptionChanged(it))
                        },
                        placeholder = stringResource(R.string.channel_description_label),
                        maxLines = 50,
                        errorMsg = descriptionErrMsg,
                        onTypingStarted = {
                            onEvent(ChannelEditingUiEvent.DescriptionTypingStarted)
                        },
                        onTypingEnded = {
                            onEvent(ChannelEditingUiEvent.DescriptionTypingEnded)
                        }
                    )
                }
                val logoPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        if (it != null) {
                            onEvent(ChannelEditingUiEvent.LogoChanged(it.toString(), UPDATE))
                        }
                    }
                val logoErrMsg = when(current.logoError) {
                    FileError.EMPTY -> stringResource(R.string.file_not_found_error)
                    FileError.LARGE -> stringResource(R.string.file_too_large_error, "${MAX_VIDEO_SIZE.mb} MB")
                    FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                    else -> null
                }
                Column {
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = current.logoAction == UPDATE || current.logoAction == REMOVE && initial.logoExists == true,
                        empty = !(current.logo != null || initial.logoExists == true && current.logoAction != REMOVE),
                        onRevert = {
                            onEvent(ChannelEditingUiEvent.LogoChanged(null, KEEP))
                        },
                        onDelete = {
                            onEvent(ChannelEditingUiEvent.LogoChanged(null, REMOVE))
                        }
                    ) {
                        FileInputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Person,
                            placeholder = if (current.logo == null) stringResource(R.string.channel_avatar_choose_label)
                            else stringResource(R.string.channel_avatar_choose_another_label),
                            onClick = {
                                logoPicker.launch("image/*")
                            },
                            errorMsg = logoErrMsg
                        )
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally)
                    ) {
                        if (initial.logoExists != false) {
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
                                    model = state.initialChannel.logo,
                                    contentDescription = current.title,
                                    onSuccess = {
                                        onEvent(ChannelEditingUiEvent.LogoExists(true))
                                    },
                                    onError = {
                                        onEvent(ChannelEditingUiEvent.LogoExists(false))
                                    }
                                )
                            }
                        }
                        if (current.logo != null) {
                            Column (
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.channel_chosen_avatar_message)
                                )
                                val painter = rememberAsyncImagePainter(model = current.logo)
                                Image(
                                    painter = painter,
                                    contentDescription = current.title,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    if (initial.logoExists == true && current.logoAction == REMOVE) {
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

                val headerPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        if (it != null) {
                            onEvent(ChannelEditingUiEvent.HeaderChanged(it.toString(), UPDATE))
                        }
                    }
                val headerErrMsg = when(current.headerError) {
                    FileError.EMPTY -> stringResource(R.string.file_not_found_error)
                    FileError.LARGE -> stringResource(R.string.file_too_large_error, "${MAX_IMAGE_SIZE.mb} MB")
                    FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                    else -> null
                }
                Column {
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = current.headerAction == UPDATE || current.headerAction == REMOVE && initial.headerExists == true,
                        empty = !(current.header != null || initial.headerExists == true && current.headerAction != REMOVE),
                        onRevert = {
                            onEvent(ChannelEditingUiEvent.LogoChanged(null, KEEP))
                        },
                        onDelete = {
                            onEvent(ChannelEditingUiEvent.LogoChanged(null, REMOVE))
                        }
                    ) {
                        FileInputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Wallpaper,
                            placeholder = if (current.header == null) stringResource(R.string.channel_choose_cover_label)
                            else stringResource(R.string.channel_choose_another_cover_label),
                            onClick = {
                                headerPicker.launch("image/*")
                            },
                            errorMsg = headerErrMsg
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
                        if (initial.headerExists != false) {
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
                                    model = state.initialChannel.header,
                                    contentDescription = current.title,
                                    onSuccess = {
                                        onEvent(ChannelEditingUiEvent.HeaderExists(true))
                                    },
                                    onError = {
                                        onEvent(ChannelEditingUiEvent.HeaderExists(true))
                                    }
                                )
                            }
                        }
                        if (current.header != null) {
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
                                val painter = rememberAsyncImagePainter(model = current.header)
                                Image(
                                    painter = painter,
                                    contentDescription = current.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        if (initial.headerExists == true && current.headerAction == REMOVE) {
                            Text(
                                text = stringResource(R.string.channel_delete_cover_warning)
                            )
                        }
                    }
                }
            }
        }
    } else if (state is ChannelEditingScreenState.Failure) {
        ErrorComponent(
            modifier = Modifier.fillMaxSize(),
            onRetry = {
                onEvent(ChannelEditingUiEvent.Retry)
            }
        )
        StandardComplexErrorHandler(
            error = state.error,
            snackBarHostState = snackBarHostState,
            notFoundMessage = stringResource(R.string.channel_not_found),
            notFoundHandler = {
                onEvent(ChannelEditingUiEvent.Cancel)
            }
        )
    } else if (state is ChannelEditingScreenState.Loading) {
        LoadingComponent(
            modifier = Modifier.fillMaxSize(),
        )
    }
}