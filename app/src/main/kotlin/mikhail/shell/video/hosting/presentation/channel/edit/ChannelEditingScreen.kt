package mikhail.shell.video.hosting.presentation.channel.edit

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TITLE_LENGTH
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenAction.ChangeAlias
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenAction.ChangeDescription
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenAction.ChangeHeader
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenAction.ChangeLogo
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenAction.ChangeTitle
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.EditingState.Editing
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.exists
import mikhail.shell.video.hosting.presentation.utils.getFileErrorMessage
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenAction as ScreenAction

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ChannelEditingScreen(
    state: ChannelEditingScreenState,
    onAction: (ScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val activity = LocalActivity.current!!
    val windowSize = calculateWindowSizeClass(activity)
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            if (state is ChannelEditingScreenState.Editing) {
                TopBar(
                    title = stringResource(R.string.channel_edit_title),
                    onPopup = {
                        onAction(ScreenAction.Cancel)
                    },
                    inProgress = state.isLoading,
                    onSubmit = {
                        onAction(ScreenAction.Submit)
                    }
                )
            } else {
                TopBar(
                    title = stringResource(R.string.channel_edit_title),
                    onPopup = {
                        onAction(ScreenAction.Cancel)
                    }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is ChannelEditingScreenState.Editing -> {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(scrollState)
                    ) {
                        val titleErrMsg = when (state.channel.title.error) {
                            TextError.EMPTY -> stringResource(R.string.text_empty_error)
                            TextError.LONG -> stringResource(
                                R.string.text_too_large_error,
                                MAX_TITLE_LENGTH
                            )
                            TextError.EXISTS -> stringResource(R.string.channel_title_exists_error)
                            is NetworkError -> stringResource(R.string.channel_title_check_error)
                            else -> null
                        }
                        StandardEditField(
                            modifier = Modifier,
                            firstTime = false,
                            edited = state.channel.title.value != state.channel.title.initial,
                            empty = state.channel.title.value.isEmpty(),
                            onRevert = {
                                onAction(ChangeTitle(state.channel.title.initial))
                            },
                            onRemove = {
                                onAction(ChangeTitle(""))
                            }
                        ) {
                            InputField(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.Title,
                                value = state.channel.title.value,
                                onValueChange = {
                                    onAction(ChangeTitle(it))
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
                            TextError.LONG -> stringResource(
                                R.string.text_too_large_error,
                                MAX_TITLE_LENGTH
                            )

                            TextError.EXISTS -> stringResource(R.string.channel_alias_exists_error)
                            is NetworkError -> stringResource(R.string.channel_alias_check_error)
                            else -> null
                        }
                        StandardEditField(
                            modifier = Modifier,
                            firstTime = false,
                            edited = state.channel.alias.value != state.channel.alias.initial,
                            empty = state.channel.alias.value.isEmpty(),
                            onRevert = {
                                onAction(ChangeAlias(state.channel.alias.initial))
                            },
                            onRemove = {
                                onAction(ChangeAlias(""))
                            },
                        ) {
                            InputField(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.AlternateEmail,
                                value = state.channel.alias.value,
                                onValueChange = {
                                    onAction(ChangeAlias(it))
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
                        StandardEditField(
                            modifier = Modifier,
                            firstTime = false,
                            edited = state.channel.description.value != state.channel.description.initial,
                            empty = state.channel.description.value.isEmpty(),
                            onRevert = {
                                onAction(ChangeDescription(state.channel.description.initial))
                            },
                            onRemove = {
                                onAction(ChangeDescription(""))
                            }
                        ) {
                            InputField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                icon = Icons.Rounded.DensityMedium,
                                value = state.channel.description.value,
                                onValueChange = {
                                    onAction(ChangeDescription(it))
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
                            rememberLauncherForActivityResult(GetContent()) {
                                if (it != null) {
                                    onAction(ChangeLogo(Editing(it.toString())))
                                }
                            }
                        val logoErrMsg = getFileErrorMessage(state.channel.logo.error)
                        val logoPainter =
                            rememberAsyncImagePainter((state.channel.logo.initial as EditingState.Keeping).value)
                        Column {
                            StandardEditField(
                                modifier = Modifier,
                                firstTime = false,
                                edited = state.channel.logo.value is EditingState.Editing || state.channel.logo.value is EditingState.Removing && logoPainter.exists() == true,
                                empty = !(state.channel.logo.value is EditingState.Editing || logoPainter.exists() == true && state.channel.logo.value !is EditingState.Removing),
                                onRevert = {
                                    onAction(ChangeLogo(state.channel.logo.initial))
                                },
                                onRemove = {
                                    onAction(ChangeLogo(EditingState.Removing))
                                }
                            ) {
                                FileInputField(
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Rounded.Person,
                                    placeholder = when (state.channel.logo.value) {
                                        !is EditingState.Editing -> stringResource(R.string.channel_logo_choose_label)
                                        else -> stringResource(R.string.channel_logo_choose_another_label)
                                    },
                                    onClick = {
                                        logoPicker.launch("image/*")
                                    },
                                    errorMsg = logoErrMsg
                                )
                            }
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    30.dp,
                                    Alignment.CenterHorizontally
                                )
                            ) {
                                if (logoPainter.exists() == true) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.channel_current_logo_message)
                                        )
                                        Image(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape),
                                            painter = logoPainter,
                                            contentScale = ContentScale.Crop,
                                            contentDescription = state.channel.title.value
                                        )
                                    }
                                }
                                if (state.channel.logo.value is EditingState.Editing) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.channel_chosen_logo_message)
                                        )
                                        AsyncImage(
                                            model = state.channel.logo.value.value,
                                            contentDescription = state.channel.title.value,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            if (logoPainter.exists() == true && state.channel.logo.value is EditingState.Removing) {
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
                            rememberLauncherForActivityResult(GetContent()) {
                                if (it != null) {
                                    onAction(ChangeHeader(Editing(it.toString())))
                                }
                            }
                        val headerErrMsg = getFileErrorMessage(state.channel.header.error)
                        val headerPainter =
                            rememberAsyncImagePainter((state.channel.header.initial as EditingState.Keeping).value)
                        Column {
                            StandardEditField(
                                modifier = Modifier,
                                firstTime = false,
                                edited = state.channel.header.value is EditingState.Editing || state.channel.header.value is EditingState.Removing && headerPainter.state is AsyncImagePainter.State.Success,
                                empty = !(state.channel.header.value is EditingState.Editing || headerPainter.state is AsyncImagePainter.State.Success && state.channel.header.value !is EditingState.Removing),
                                onRevert = {
                                    onAction(ChangeHeader(state.channel.header.initial))
                                },
                                onRemove = {
                                    onAction(ChangeHeader(EditingState.Removing))
                                }
                            ) {
                                FileInputField(
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Rounded.Wallpaper,
                                    placeholder = when (state.channel.header.value) {
                                        !is EditingState.Editing -> stringResource(R.string.channel_choose_header_label)
                                        else -> stringResource(R.string.channel_choose_another_header_label)
                                    },
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
                                if (headerPainter.state is AsyncImagePainter.State.Success) {
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
                                        Image(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            painter = headerPainter,
                                            contentScale = ContentScale.Crop,
                                            contentDescription = state.channel.title.value
                                        )
                                    }
                                }
                                if (state.channel.header.value is EditingState.Editing<*>) {
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
                                            model = state.channel.header.value.value,
                                            contentDescription = state.channel.title.value,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                if (headerPainter.state is AsyncImagePainter.State.Success && state.channel.header.value is EditingState.Removing) {
                                    Text(
                                        text = stringResource(R.string.channel_delete_header_warning)
                                    )
                                }
                            }
                        }
                    }

                }
                is ChannelEditingScreenState.Failure -> {
                    ErrorComponent(
                        modifier = Modifier.fillMaxSize(),
                        onRetry = {
                            onAction(ScreenAction.Restart)
                        }
                    )
                }
                is ChannelEditingScreenState.Starting -> {
                    StartingComponent(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ChannelEditingScreenState.Idle -> Unit
            }
        }
    }
}