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
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_IMAGE_SIZE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TITLE_LENGTH
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
fun VideoEditingScreen(
    state: VideoEditingScreenState,
    onEvent: (VideoEditingUiEvent) -> Unit
) {
    val activity = LocalActivity.current!!
    val windowSize = calculateWindowSizeClass(activity)
    val scrollState = rememberScrollState()
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) { padding ->
        if (state is VideoEditingScreenState.Editing) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                TopBar(
                    title = stringResource(R.string.video_edit_title),
                    onPopup = {
                        onEvent(VideoEditingUiEvent.Cancel)
                    },
                    inProgress = state.isLoading,
                    complete = false,
                    onSubmit = {
                        onEvent(VideoEditingUiEvent.Submit)
                    }
                )
                val titleErrMsg = when (state.currentVideo.titleError) {
                    TextError.EMPTY -> stringResource(R.string.text_empty_error)
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                    else -> null
                }
                StandardEditField(
                    firstTime = false,
                    updated = state.currentVideo.title != state.initialVideo.title,
                    empty = state.currentVideo.title.isEmpty(),
                    onDelete = {
                        onEvent(VideoEditingUiEvent.TitleChanged(""))
                    },
                    onRevert = {
                        onEvent(VideoEditingUiEvent.TitleChanged(state.initialVideo.title))
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.currentVideo.title,
                        onValueChange = {
                            onEvent(VideoEditingUiEvent.TitleChanged(it))
                        },
                        errorMsg = titleErrMsg,
                        placeholder = stringResource(R.string.video_title_label),
                        icon = Icons.Rounded.Title,
                        onTypingStarted = {
                            onEvent(VideoEditingUiEvent.TitleTypingStarted)
                        },
                        onTypingEnded = {
                            onEvent(VideoEditingUiEvent.TitleTypingEnded)
                        }
                    )
                }
                val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                    if (it != null) {
                        onEvent(VideoEditingUiEvent.CoverChanged(it.toString(), UPDATE))
                    }
                }
                var coverExists by rememberSaveable { mutableStateOf<Boolean?>(null) }
                val coverErrMsg = when (state.currentVideo.coverError) {
                    FileError.EMPTY -> stringResource(R.string.file_not_found_error)
                    FileError.LARGE -> stringResource(R.string.file_too_large_error, "${(MAX_IMAGE_SIZE.mb)} MB")
                    FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                    else -> null
                }
                Column {
                    StandardEditField(
                        firstTime = false,
                        updated = state.currentVideo.coverAction == state.currentVideo.coverAction || state.currentVideo.coverAction == REMOVE && coverExists == true,
                        empty = !(state.currentVideo.cover != null || coverExists == true && state.currentVideo.coverAction != REMOVE),
                        onRevert = {
                            onEvent(VideoEditingUiEvent.CoverChanged(null, KEEP))

                        },
                        onDelete = {
                            onEvent(VideoEditingUiEvent.CoverChanged(null, REMOVE))
                        }
                    ) {
                        FileInputField(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                coverPicker.launch("image/*")
                            },
                            placeholder = if (state.currentVideo.cover != null || coverExists == true && state.currentVideo.coverAction == KEEP)
                                stringResource(R.string.video_cover_choose_another_label)
                            else stringResource(R.string.video_cover_choose_label),
                            icon = Icons.Rounded.Wallpaper,
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
                                    text = stringResource(R.string.video_cover_current_label)
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                    model = state.initialVideo.cover,
                                    contentDescription = state.initialVideo.title,
                                    onSuccess = { coverExists = true },
                                    onError = { coverExists = false }
                                )
                            }
                        }
                        if (state.currentVideo.cover != null) {
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
                                val painter = rememberAsyncImagePainter(model = state.currentVideo.cover)
                                Image(
                                    painter = painter,
                                    contentDescription = state.initialVideo.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    if (coverExists == true && state.currentVideo.coverAction == REMOVE) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.video_cover_delete_warning)
                            )
                        }
                    }
                }
                val descriptionErrMsg = when (state.currentVideo.descriptionError) {
                    TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                    else -> null
                }
                StandardEditField(
                    firstTime = false,
                    updated = state.currentVideo.description != state.initialVideo.description,
                    empty = state.currentVideo.description.isEmpty(),
                    onDelete = {
                        onEvent(VideoEditingUiEvent.DescriptionChanged(""))
                    },
                    onRevert = {
                        onEvent(VideoEditingUiEvent.DescriptionChanged(state.initialVideo.description))
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.currentVideo.description,
                        onValueChange = {
                            onEvent(VideoEditingUiEvent.TitleChanged(it))
                        },
                        errorMsg = descriptionErrMsg,
                        placeholder = stringResource(R.string.video_title_label),
                        icon = Icons.Rounded.Title,
                        onTypingStarted = {
                            onEvent(VideoEditingUiEvent.DescriptionTypingStarted)
                        },
                        onTypingEnded = {
                            onEvent(VideoEditingUiEvent.DescriptionTypingEnded)
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
                if (state is VideoEditingScreenState.Loading) {
                    LoadingComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                } else if (state is VideoEditingScreenState.Failure) {
                    ErrorComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        onRetry = {
                            onEvent(VideoEditingUiEvent.Reload)
                        }
                    )
                    StandardComplexErrorHandler(
                        error = state.error,
                        snackBarHostState = snackBarHostState,
                        notFoundMessage = stringResource(R.string.video_not_found),
                        notFoundHandler = {
                            onEvent(VideoEditingUiEvent.Cancel)
                        }
                    )
                }
            }
        }
    }
}