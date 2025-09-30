package mikhail.shell.video.hosting.presentation.channel.edit

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TITLE_LENGTH
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorDisplay
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.getFileErrorMessage

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
                val titleErrMsg = when(current.title.error) {
                    TextError.EMPTY -> stringResource(R.string.text_empty_error)
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                    TextError.EXISTS -> stringResource(R.string.channel_title_exists_error)
                    is NetworkError -> stringResource(R.string.channel_title_check_error)
                    else -> null
                }
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = current.title.value != initial.title,
                    empty = current.title.value.isEmpty(),
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
                        value = current.title.value,
                        onValueChange = {
                            onEvent(ChannelEditingUiEvent.TitleChanged(it))
                        },
                        placeholder = stringResource(R.string.channel_title_label),
                        errorMsg = titleErrMsg,
                        onFocus = {
                            onEvent(ChannelEditingUiEvent.TitleFocused)
                        },
                        onBlur = {
                            onEvent(ChannelEditingUiEvent.TitleBlurred)
                        }
                    )
                }
                val aliasErrMsg = when (current.alias.error) {
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                    TextError.EXISTS -> stringResource(R.string.channel_alias_exists_error)
                    is NetworkError -> stringResource(R.string.channel_alias_check_error)
                    else -> null
                }
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = current.alias.value != initial.alias,
                    empty = current.alias.value.isEmpty(),
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
                        value = current.alias.value,
                        onValueChange = {
                            onEvent(ChannelEditingUiEvent.AliasChanged(it))
                        },
                        placeholder = stringResource(R.string.channel_alias_label),
                        errorMsg = aliasErrMsg,
                        onFocus = {
                            onEvent(ChannelEditingUiEvent.AliasFocused)
                        },
                        onBlur = {
                            onEvent(ChannelEditingUiEvent.AliasBlurred)
                        }
                    )
                }
                val descriptionErrMsg = when (current.description.error) {
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TEXT_LENGTH)
                    else -> null
                }
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = current.description.value != initial.description,
                    empty = current.description.value.isEmpty(),
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
                        value = current.description.value,
                        onValueChange = {
                            onEvent(ChannelEditingUiEvent.DescriptionChanged(it))
                        },
                        placeholder = stringResource(R.string.channel_description_label),
                        maxLines = 50,
                        errorMsg = descriptionErrMsg,
                        onFocus = {
                            onEvent(ChannelEditingUiEvent.DescriptionFocused)
                        },
                        onBlur = {
                            onEvent(ChannelEditingUiEvent.DescriptionBlurred)
                        }
                    )
                }
                val logoPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        if (it != null) {
                            onEvent(ChannelEditingUiEvent.LogoChanged(it.toString(), UPDATE))
                        }
                    }
                val logoErrMsg = getFileErrorMessage(current.logo.error)
                Column {
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = current.logoAction == UPDATE || current.logoAction == REMOVE && initial.logoExists == true,
                        empty = !(current.logo.value != null || initial.logoExists == true && current.logoAction != REMOVE),
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
                            placeholder = if (current.logo.value == null) stringResource(R.string.channel_logo_choose_label)
                            else stringResource(R.string.channel_logo_choose_another_label),
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
                                    text = stringResource(R.string.channel_current_logo_message)
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    model = state.initialChannel.logo,
                                    contentDescription = current.title.value,
                                    onSuccess = {
                                        onEvent(ChannelEditingUiEvent.LogoExists(true))
                                    },
                                    onError = {
                                        onEvent(ChannelEditingUiEvent.LogoExists(false))
                                    }
                                )
                            }
                        }
                        if (current.logo.value != null) {
                            Column (
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.channel_chosen_logo_message)
                                )
                                AsyncImage(
                                    model = current.logo.value,
                                    contentDescription = current.title.value,
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
                                text = stringResource(R.string.channel_delete_logo_message)
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
                val headerErrMsg = getFileErrorMessage(current.header.error)
                Column {
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = current.headerAction == UPDATE || current.headerAction == REMOVE && initial.headerExists == true,
                        empty = !(current.header.value != null || initial.headerExists == true && current.headerAction != REMOVE),
                        onRevert = {
                            onEvent(ChannelEditingUiEvent.HeaderChanged(null, KEEP))
                        },
                        onDelete = {
                            onEvent(ChannelEditingUiEvent.HeaderChanged(null, REMOVE))
                        }
                    ) {
                        FileInputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Wallpaper,
                            placeholder = if (current.header.value == null) stringResource(R.string.channel_choose_header_label)
                            else stringResource(R.string.channel_choose_another_header_label),
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
                                    text = stringResource(R.string.channel_current_header_message)
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                    model = state.initialChannel.header,
                                    contentDescription = current.title.value,
                                    onSuccess = {
                                        onEvent(ChannelEditingUiEvent.HeaderExists(true))
                                    },
                                    onError = {
                                        onEvent(ChannelEditingUiEvent.HeaderExists(false))
                                    }
                                )
                            }
                        }
                        if (current.header.value != null) {
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
                                    text = stringResource(R.string.channel_chosen_header_message)
                                )
                                AsyncImage(
                                    model = current.header.value,
                                    contentDescription = current.title.value,
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
                                text = stringResource(R.string.channel_delete_header_warning)
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
                onEvent(ChannelEditingUiEvent.Restart)
            }
        )
        ErrorDisplay(
            error = state.error,
            snackBarHostState = snackBarHostState
        )
    } else if (state is ChannelEditingScreenState.Starting) {
        LoadingComponent(
            modifier = Modifier.fillMaxSize()
        )
    }
}