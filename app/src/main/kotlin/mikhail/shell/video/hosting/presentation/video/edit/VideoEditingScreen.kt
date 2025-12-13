package mikhail.shell.video.hosting.presentation.video.edit

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.ViewHeadline
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TITLE_LENGTH
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.getFileErrorMessage

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun VideoEditingScreen(
    state: VideoEditingScreenState,
    onAction: (VideoEditingScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val activity = LocalActivity.current!!
    val windowSize = calculateWindowSizeClass(activity)
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { padding ->
        if (state is VideoEditingScreenState.Editing) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                TopBar(
                    title = stringResource(R.string.video_edit_title),
                    onPopup = { onAction(VideoEditingScreenAction.Cancel) },
                    inProgress = state.isLoading,
                    complete = false,
                    onSubmit = { onAction(VideoEditingScreenAction.Submit) }
                )
                val titleErrMsg = when (state.video.title.error) {
                    TextError.EMPTY -> stringResource(R.string.text_empty_error)
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                    else -> null
                }
                StandardEditField(
                    firstTime = false,
                    updated = state.video.title.value != state.video.title.initial,
                    empty = state.video.title.value.isEmpty(),
                    onDelete = {
                        onAction(VideoEditingScreenAction.ChangeTitle(""))
                    },
                    onRevert = {
                        onAction(VideoEditingScreenAction.ChangeTitle(state.video.title.initial))
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.video.title.value,
                        onValueChange = {
                            onAction(VideoEditingScreenAction.ChangeTitle(it))
                        },
                        errorMsg = titleErrMsg,
                        label = stringResource(R.string.video_title_label),
                        icon = Icons.Rounded.Title,
                        onFocus = {
                            onAction(VideoEditingScreenAction.FocusTitle)
                        },
                        onBlur = {
                            onAction(VideoEditingScreenAction.BlurTitle)
                        }
                    )
                }
                val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                    if (it != null) {
                        onAction(VideoEditingScreenAction.ChangeCover(EditingState.Editing(it.toString())))
                    }
                }
                val coverErrMsg = getFileErrorMessage(state.video.cover.error)
                val coverPainter = rememberAsyncImagePainter((state.video.cover.initial as EditingState.Keeping<String?>).value)
                val coverExists by rememberSaveable {
                    derivedStateOf {
                        when (coverPainter.state) {
                            is AsyncImagePainter.State.Error -> false
                            is AsyncImagePainter.State.Success -> true
                            else -> null
                        }
                    }
                }
                Column {
                    StandardEditField(
                        firstTime = false,
                        updated = state.video.cover.value is EditingState.Editing || state.video.cover.value is EditingState.Removing && coverExists == true,
                        empty = !(state.video.cover.value is EditingState.Editing || state.video.cover.value !is EditingState.Removing && coverExists == true),
                        onRevert = {
                            onAction(VideoEditingScreenAction.ChangeCover(state.video.cover.initial))
                        },
                        onDelete = {
                            onAction(VideoEditingScreenAction.ChangeCover(EditingState.Removing))
                        }
                    ) {
                        FileInputField(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { coverPicker.launch("image/*") },
                            placeholder = when (state.video.cover.value) {
                                !is EditingState.Editing -> stringResource(R.string.video_cover_choose_another_label)
                                else -> stringResource(R.string.video_cover_choose_label)
                            },
                            icon = Icons.Rounded.Wallpaper,
                            errorMsg = coverErrMsg
                        )
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 30.dp,
                            alignment = Alignment.CenterHorizontally
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
                                    text = stringResource(R.string.video_cover_current_label)
                                )
                                Image(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9)
                                        .clip(RoundedCornerShape(10.dp)),
                                    painter = coverPainter,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = state.video.title.value,
                                )
                            }
                        }
                        if (state.video.cover.value is EditingState.Editing) {
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
                                    text = stringResource(R.string.video_cover_chosen_label)
                                )
                                AsyncImage(
                                    model = state.video.cover.value.value,
                                    contentDescription = state.video.title.value,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    if (coverExists == true && state.video.cover.value is EditingState.Removing) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.video_cover_delete_warning)
                            )
                        }
                    }
                }
                val descriptionErrMsg = when (state.video.description.error) {
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                    else -> null
                }
                StandardEditField(
                    firstTime = false,
                    updated = state.video.description.value != state.video.description.initial,
                    empty = state.video.description.value.isEmpty(),
                    onDelete = {
                        onAction(VideoEditingScreenAction.ChangeDescription(""))
                    },
                    onRevert = {
                        onAction(VideoEditingScreenAction.ChangeDescription(state.video.description.initial))
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.video.description.value,
                        onValueChange = { onAction(VideoEditingScreenAction.ChangeDescription(it)) },
                        errorMsg = descriptionErrMsg,
                        label = stringResource(R.string.video_description_label),
                        icon = Icons.Rounded.ViewHeadline,
                        onFocus = {
                            onAction(VideoEditingScreenAction.FocusDescription)
                                  },
                        onBlur = {
                            onAction(VideoEditingScreenAction.BlurDescription)
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state is VideoEditingScreenState.Starting) {
                    StartingComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                } else if (state is VideoEditingScreenState.Failure) {
                    ErrorComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        onRetry = { onAction(VideoEditingScreenAction.Restart) }
                    )
                }
            }
        }
    }
}