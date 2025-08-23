package mikhail.shell.video.hosting.presentation.video.upload

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Environment
import android.view.WindowInsetsController
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.OptionError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_IMAGE_SIZE
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
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.TopBar
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun VideoUploadingScreen(
    state: VideoUploadingScreenState,
    player: Player,
    onEvent: (VideoUploadingScreenUiEvent) -> Unit
) {
    val activity = LocalActivity.current!!
    val playerState = LocalPlayerState.current
    val windowSize = calculateWindowSizeClass(activity)
    val context = activity as Context
    val coroutineScope = rememberCoroutineScope()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(activity, arrayOf("android.permission.POST_NOTIFICATIONS"), 0)
    }
    val scrollState = rememberScrollState()
    when (state) {
        is VideoUploadingScreenState.Editing -> {
            var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
            var isFullScreen by rememberSaveable { mutableStateOf(false) }
            val snackBarHostState = remember { SnackbarHostState() }
            Scaffold(
                topBar = {
                    if (!isFullScreen) {
                        TopBar(
                            onPopup = {
                                onEvent(VideoUploadingScreenUiEvent.Cancel)
                            },
                            title = stringResource(R.string.video_upload_title),
                            inProgress = state.isLoading,
                            complete = false,
                            onSubmit = {
                                onEvent(VideoUploadingScreenUiEvent.Submit)
                            }
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(snackBarHostState)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
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
                        val sourceCreator = rememberLauncherForActivityResult(
                            ActivityResultContracts.CaptureVideo()
                        ) {
                            if (it) {
                                val newMediaItem = MediaItem.fromUri(state.input.source!!)
                                player.setMediaItem(newMediaItem)
                                player.prepare()
                            }
                        }
                        val sourcePicker = rememberLauncherForActivityResult(
                            ActivityResultContracts.GetContent()
                        ) {
                            if (it != null) {
                                onEvent(VideoUploadingScreenUiEvent.SourceChanged(it.toString()))
                                val newMediaItem = MediaItem.fromUri(it)
                                player.setMediaItem(newMediaItem)
                                player.prepare()
                            }
                        }
                        val sourceErrMsg = when (state.input.sourceError) {
                            FileError.EMPTY -> stringResource(R.string.video_upload_source_empty)
                            FileError.LARGE -> stringResource(R.string.file_too_large_error, "${MAX_VIDEO_SIZE.mb} MB")
                            FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                            else -> null
                        }
                        val sourceActionItems = if (state.input.source == null) listOf()
                        else listOf(
                            DeletingItem(
                                deleting = {
                                    onEvent(VideoUploadingScreenUiEvent.SourceChanged(null))
                                }
                            )
                        )
                        var isVideoDialogOpen by rememberSaveable { mutableStateOf(false) }
                        Row {
                            EditField(
                                actionItems = sourceActionItems
                            ) {
                                FileInputField(
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = when (state.input.source) {
                                        state.input.source -> stringResource(R.string.video_upload_choose_source_label)
                                        else -> stringResource(R.string.video_upload_choose_another_source_label)
                                    },
                                    onClick = {
                                        isVideoDialogOpen = true
                                    },
                                    icon = Icons.Rounded.VideoLibrary,
                                    errorMsg = sourceErrMsg
                                )
                            }
                            val recordedVideoDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
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
                                            val isCameraPermissionGranted =
                                                cameraPermission.status.isGranted
                                            if (isCameraPermissionGranted) {
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
                                                onEvent(VideoUploadingScreenUiEvent.SourceChanged(uri.toString()))
                                                sourceCreator.launch(uri)
                                            } else {
                                                if (ActivityCompat
                                                    .shouldShowRequestPermissionRationale(
                                                        context as Activity,
                                                        "android.permission.CAMERA")
                                                    ) {
                                                    coroutineScope.launch {
                                                        snackBarHostState.showSnackbar(
                                                            message = context.getString(R.string.video_upload_camera_permission_rationale),
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } else {
                                                    ActivityCompat.requestPermissions(
                                                        context,
                                                        arrayOf("android.permission.CAMERA"),
                                                        0
                                                    )
                                                }
                                            }
                                        }
                                    ),
                                    MenuItem(
                                        title = stringResource(R.string.video_upload_choose_source_label),
                                        onClick = { sourcePicker.launch("video/*") }
                                    )
                                )
                            )
                        }
                    }
                    LaunchedEffect(state.input.source) {
                        if (state.input.source == null) {
                            player.stop()
                            player.clearMediaItems()
                        }
                    }
                    if (state.input.source != null) {
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
                        val titleErrMsg = when(state.input.titleError) {
                            TextError.EMPTY -> stringResource(R.string.text_empty_error)
                            TextError.LARGE -> stringResource(R.string.text_too_large_error, MAX_TITLE_LENGTH)
                            else -> null
                        }
                        val titleActionItems = if (state.input.title.isBlank()) emptyList() else listOf(
                            ActionItem(
                                icon = Icons.Rounded.Delete,
                                action = {
                                    onEvent(VideoUploadingScreenUiEvent.TitleChanged(""))
                                }
                            )
                        )
                        EditField(
                            actionItems = titleActionItems
                        ) {
                            InputField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.input.title,
                                onValueChange = {
                                    onEvent(VideoUploadingScreenUiEvent.TitleChanged(it))
                                },
                                errorMsg = titleErrMsg,
                                placeholder = stringResource(R.string.video_title_label),
                                icon = Icons.Rounded.Title
                            )
                        }

                        val channelErrMsg = when (state.input.channelError) {
                            OptionError.EMPTY -> stringResource(R.string.video_upload_channel_not_valid_error)
                            else -> null
                        }
                        val channelActionItems = if (state.input.channelId == null) emptyList() else listOf(
                            ActionItem(
                                icon = Icons.Rounded.Delete,
                                action = {
                                    onEvent(VideoUploadingScreenUiEvent.ChannelChanged(null))
                                }
                            )
                        )
                        EditField(
                            actionItems = channelActionItems
                        ) {
                            Dropdown(
                                selected = state.input.channelId,
                                modifier = Modifier.fillMaxWidth(),
                                placeHolder = stringResource(R.string.video_upload_channel_label),
                                values = state.channels.associate { it.channelId to it.title },
                                onValueChange = {
                                    onEvent(VideoUploadingScreenUiEvent.ChannelChanged(it))
                                },
                                errorMsg = channelErrMsg,
                                icon = Icons.Rounded.Apps,
                            )
                        }
                        val coverPicker = rememberLauncherForActivityResult(
                            ActivityResultContracts.GetContent()
                        ) {
                            if (it != null) {
                                onEvent(VideoUploadingScreenUiEvent.CoverChanged(it.toString()))
                            }
                        }
                        val coverErrMsg = when (state.input.coverError) {
                            FileError.EMPTY -> stringResource(R.string.file_not_found_error)
                            FileError.LARGE -> stringResource(R.string.file_too_large_error, "${MAX_IMAGE_SIZE.mb} MB")
                            FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                            null -> TODO()
                        }
                        EditField(
                            actionItems = if (state.input.cover == null) emptyList() else listOf(
                                ActionItem(
                                    icon = Icons.Rounded.Delete,
                                    action = {
                                        onEvent(VideoUploadingScreenUiEvent.CoverChanged(null))
                                    }
                                )
                            )
                        ) {
                            FileInputField(
                                placeholder = if (state.input.cover == null) stringResource(R.string.video_cover_choose_label)
                                else stringResource(R.string.video_cover_choose_another_label),
                                onClick = {
                                    coverPicker.launch("image/*")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.Image,
                                errorMsg = coverErrMsg
                            )
                        }
                        if (state.input.cover != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.video_cover_chosen_label)
                                )
                                Image(
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
                                    contentScale = ContentScale.Crop,
                                    painter = rememberAsyncImagePainter(state.input.cover),
                                    contentDescription = stringResource(R.string.video_cover_chosen_label)
                                )
                            }
                        }
                    }
                }
            }
        }

        is VideoUploadingScreenState.Failure -> {
            ErrorComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                onRetry = {
                    onEvent(VideoUploadingScreenUiEvent.Reload)
                }
            )
        }

        is VideoUploadingScreenState.Loading -> {
            LoadingComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        }

        else -> {
            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text (
                    text = stringResource(R.string.video_uploaded_success)
                )
            }
        }
    }
}
