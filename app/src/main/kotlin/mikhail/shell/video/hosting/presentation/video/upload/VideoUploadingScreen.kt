package mikhail.shell.video.hosting.presentation.video.upload

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import android.view.WindowInsetsController
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CaptureVideo
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.NumericError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TITLE_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_VIDEO_SIZE
import mikhail.shell.video.hosting.domain.validation.mb
import mikhail.shell.video.hosting.presentation.exoplayer.LocalPlayerState
import mikhail.shell.video.hosting.presentation.exoplayer.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.ActionItem
import mikhail.shell.video.hosting.presentation.utils.ContextMenu
import mikhail.shell.video.hosting.presentation.utils.DeletingItem
import mikhail.shell.video.hosting.presentation.utils.Dropdown
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.getFileErrorMessage
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.ChangeChannel
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.ChangeCover
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.ChangeDescription
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.ChangeSource
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction.ChangeTitle
import java.io.File
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.video.upload.VideoUploadingScreenState as ScreenState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun VideoUploadingScreen(
    state: ScreenState,
    player: Player,
    onAction: (ScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val activity = LocalActivity.current!!
    val playerState = LocalPlayerState.current
    val windowSize = calculateWindowSizeClass(activity)
    val context = activity as Context
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf("android.permission.POST_NOTIFICATIONS"),
                0
            )
        }
    }
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            if (!isFullScreen) {
                if (state is ScreenState.Editing) {
                    TopBar(
                        onPopup = {
                            onAction(ScreenAction.Cancel)
                        },
                        title = stringResource(R.string.video_upload_title),
                        inProgress = state.isLoading,
                        onSubmit = {
                            onAction(ScreenAction.Submit)
                        }
                    )
                } else {
                    TopBar(
                        onPopup = {
                            onAction(ScreenAction.Cancel)
                        },
                        title = stringResource(R.string.video_upload_title)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is ScreenState.Editing -> {
                    val scrollState = rememberScrollState()
                    var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .background(MaterialTheme.colorScheme.background)
                            .then(
                                if (isFullScreen) {
                                    Modifier
                                } else {
                                    Modifier.verticalScroll(scrollState)
                                }
                            )
                    ) {
                        if (!isFullScreen) {
                            val sourceCreator =
                                rememberLauncherForActivityResult(CaptureVideo()) {
                                    if (it) {
                                        onAction(ChangeSource(state.video.source.value))
                                    }
                                }
                            val sourcePicker =
                                rememberLauncherForActivityResult(PickVisualMedia()) {
                                    if (it != null) {
                                        onAction(ChangeSource(it.toString()))
                                    }
                                }
                            val sourceErrMsg = when (state.video.source.error) {
                                FileError.EMPTY -> stringResource(R.string.video_upload_source_empty)
                                FileError.NOT_FOUND -> stringResource(R.string.file_not_found_error)
                                FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                                FileError.LARGE -> stringResource(
                                    R.string.file_too_large_error,
                                    "${MAX_VIDEO_SIZE.mb} MB"
                                )

                                else -> null
                            }
                            val sourceActionItems = when (state.video.source.value) {
                                null -> listOf()
                                else -> listOf(
                                    DeletingItem(
                                        deleting = {
                                            onAction(ChangeSource(null))
                                        }
                                    )
                                )
                            }
                            var isVideoDialogOpen by rememberSaveable { mutableStateOf(false) }
                            Row {
                                EditField(
                                    actionItems = sourceActionItems
                                ) {
                                    FileInputField(
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = when (state.video.source.value) {
                                            null -> stringResource(R.string.video_upload_choose_source_label)
                                            else -> stringResource(R.string.video_upload_choose_another_source_label)
                                        },
                                        onClick = {
                                            isVideoDialogOpen = true
                                        },
                                        icon = Icons.Rounded.VideoLibrary,
                                        errorMsg = sourceErrMsg
                                    )
                                }
                                val recordedVideoDir =
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
                                ContextMenu(
                                    isExpanded = isVideoDialogOpen,
                                    onDismiss = {
                                        isVideoDialogOpen = false
                                    },
                                    menuItems = listOf(
                                        MenuItem(
                                            title = stringResource(R.string.video_upload_create_source_label),
                                            onClick = {
                                                if (cameraPermission.status.isGranted) {
                                                    val file = File(
                                                        recordedVideoDir,
                                                        "${System.currentTimeMillis()}.mp4"
                                                    )
                                                    file.createNewFile()
                                                    val uri = FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.fileprovider",
                                                        file
                                                    )
                                                    onAction(ChangeSource(uri.toString()))
                                                    sourceCreator.launch(uri)
                                                } else if (cameraPermission.status.shouldShowRationale) {
                                                    onAction(
                                                        ScreenAction.ShowPermissionLack(
                                                            context.getString(
                                                                R.string.video_upload_camera_permission_rationale
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    cameraPermission.launchPermissionRequest()
                                                }
                                            }
                                        ),
                                        MenuItem(
                                            title = stringResource(R.string.video_upload_choose_source_label),
                                            onClick = {
                                                sourcePicker.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.VideoOnly
                                                    )
                                                )
                                            }
                                        )
                                    )
                                )
                            }
                        }
                        LaunchedEffect(state.video.source.value) {
                            if (state.video.source.value != null) {
                                val newMediaItem = MediaItem.fromUri(state.video.source.value)
                                player.setMediaItem(newMediaItem)
                                player.prepare()
                            } else {
                                player.stop()
                                player.clearMediaItems()
                            }
                        }
                        if (state.video.source.value != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isFullScreen) {
                                            Modifier.fillMaxHeight()
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                PlayerComponent(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isFullScreen) {
                                                Modifier.fillMaxSize()
                                            } else {
                                                try {
                                                    Modifier.aspectRatio(if (aspectRatio < 1f) 16f / 9 else aspectRatio)
                                                } catch (_: IllegalArgumentException) {
                                                    Modifier.aspectRatio(16f / 9)
                                                }
                                            }
                                        ),
                                    player = player,
                                    onRatioObtained = {
                                        aspectRatio = it
                                    },
                                    isFullScreen = isFullScreen,
                                    onFullscreen = {
                                        isFullScreen = it
                                        playerState.value = playerState.value.copy(fullScreen = it)
                                    }
                                )
                            }
                            LaunchedEffect(isFullScreen) {
                                playerState.value = playerState.value.copy(fullScreen = isFullScreen)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val window = activity.window!!
                                    WindowCompat.setDecorFitsSystemWindows(window, !isFullScreen)
                                    if (isFullScreen) {
                                        window.insetsController?.let {
                                            it.hide(WindowInsetsCompat.Type.systemBars())
                                            it.systemBarsBehavior =
                                                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                                        }
                                    } else {
                                        window.insetsController?.show(WindowInsetsCompat.Type.systemBars())
                                    }
                                }
                            }
                        }
                        if (!isFullScreen) {
                            val titleErrMsg = when (state.video.title.error) {
                                TextError.EMPTY -> stringResource(R.string.text_empty_error)
                                TextError.LONG -> stringResource(
                                    R.string.text_too_large_error,
                                    MAX_TITLE_LENGTH
                                )

                                else -> null
                            }
                            val titleActionItems =
                                if (state.video.title.value.isEmpty()) emptyList() else listOf(
                                    ActionItem(
                                        icon = Icons.Rounded.Delete,
                                        action = {
                                            onAction(ChangeTitle(""))
                                        }
                                    )
                                )
                            EditField(
                                actionItems = titleActionItems
                            ) {
                                InputField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = state.video.title.value,
                                    onValueChange = {
                                        onAction(ChangeTitle(it))
                                    },
                                    errorMsg = titleErrMsg,
                                    label = stringResource(R.string.video_title_label),
                                    icon = Icons.Rounded.Title,
                                    onFocus = {
                                        onAction(ScreenAction.FocusTitle)
                                    },
                                    onBlur = {
                                        onAction(ScreenAction.BlurTitle)
                                    }
                                )
                            }

                            val channelErrMsg = when (state.video.channelId.error) {
                                NumericError.EMPTY -> stringResource(R.string.video_upload_channel_not_valid_error)
                                NumericError.NOT_EXISTS -> stringResource(R.string.channel_not_found)
                                else -> null
                            }
                            val channelActionItems = when (state.video.channelId.value) {
                                null -> emptyList()
                                else -> listOf(
                                    ActionItem(
                                        icon = Icons.Rounded.Delete,
                                        action = {
                                            onAction(ChangeChannel(null))
                                        }
                                    )
                                )
                            }
                            EditField(
                                actionItems = channelActionItems
                            ) {
                                Dropdown(
                                    selected = state.video.channelId.value,
                                    modifier = Modifier.fillMaxWidth(),
                                    placeHolder = stringResource(R.string.video_upload_channel_label),
                                    values = state.channels.associate { it.channelId to it.title },
                                    onValueChange = {
                                        onAction(ChangeChannel(it))
                                    },
                                    errorMsg = channelErrMsg,
                                    icon = Icons.Rounded.Apps,
                                )
                            }
                            val coverPicker = rememberLauncherForActivityResult(
                                GetContent()
                            ) {
                                if (it != null) {
                                    onAction(ChangeCover(it.toString()))
                                }
                            }
                            val coverErrMsg = getFileErrorMessage(state.video.cover.error)
                            EditField(
                                actionItems = when (state.video.cover.value) {
                                    null -> emptyList()
                                    else -> listOf(
                                        ActionItem(
                                            icon = Icons.Rounded.Delete,
                                            action = {
                                                onAction(ChangeCover(null))
                                            }
                                        )
                                    )
                                }
                            ) {
                                FileInputField(
                                    placeholder = when (state.video.cover.value) {
                                        state.video.cover.value -> stringResource(R.string.video_cover_choose_label)
                                        else -> stringResource(R.string.video_cover_choose_another_label)
                                    },
                                    onClick = {
                                        coverPicker.launch("image/*")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Rounded.Image,
                                    errorMsg = coverErrMsg
                                )
                            }
                            if (state.video.cover.value != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.video_cover_chosen_label)
                                    )
                                    AsyncImage(
                                        modifier = Modifier
                                            .then(
                                                if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                                                    Modifier.fillMaxWidth()
                                                } else {
                                                    Modifier.width(350.dp)
                                                }
                                            )
                                            .aspectRatio(16f / 9)
                                            .clip(RoundedCornerShape(10.dp)),
                                        model = state.video.cover.value,
                                        contentDescription = stringResource(R.string.video_cover_chosen_label),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            val descriptionErrMsg = when (state.video.description.error) {
                                TextError.LONG -> stringResource(
                                    R.string.text_too_large_error,
                                    MAX_TITLE_LENGTH
                                )

                                else -> null
                            }
                            StandardEditField(
                                empty = state.video.description.value.isEmpty(),
                                onRemove = {
                                    onAction(ChangeDescription(""))
                                }
                            ) {
                                InputField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    value = state.video.description.value,
                                    onValueChange = {
                                        onAction(ChangeDescription(it))
                                    },
                                    maxLines = 50,
                                    errorMsg = descriptionErrMsg,
                                    label = stringResource(R.string.video_description_label),
                                    icon = Icons.Rounded.Title,
                                    onFocus = {
                                        onAction(ScreenAction.FocusDescription)
                                    },
                                    onBlur = {
                                        onAction(ScreenAction.BlurDescription)
                                    }
                                )
                            }
                        }
                    }

                }

                is ScreenState.Failure -> {
                    ErrorComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        onRetry = {
                            onAction(ScreenAction.Restart)
                        }
                    )
                }

                is ScreenState.Starting -> {
                    StartingComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }

                ScreenState.Idle -> Unit
            }
        }
    }
}
