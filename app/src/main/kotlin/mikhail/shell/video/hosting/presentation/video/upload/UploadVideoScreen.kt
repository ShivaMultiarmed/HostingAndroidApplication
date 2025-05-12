package mikhail.shell.video.hosting.presentation.video.upload

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Video
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UploadVideoScreen(
    modifier: Modifier = Modifier,
    state: UploadVideoScreenState,
    player: Player,
    onSubmit: (UploadVideoInput) -> Unit,
    onRefresh: () -> Unit,
    onSuccess: (Video) -> Unit,
    onPopup: () -> Unit = {},
    onFullScreen: (Boolean) -> Unit
) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf("android.permission.POST_NOTIFICATIONS"),
            0
        )
    }
    val scrollState = rememberScrollState()
    val compoundError = state.error
    if (state.channels != null) {
        var aspectRatio by rememberSaveable { mutableFloatStateOf(16f / 9) }
        var isFullScreen by rememberSaveable { mutableStateOf(false) }
        val orientation = LocalConfiguration.current.orientation
        val snackbarHostState = remember { SnackbarHostState() }
        var title by rememberSaveable { mutableStateOf("") }
        var sourceUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var channelId by rememberSaveable { mutableStateOf<Long?>(null) }
        Scaffold(
            topBar = {
                if (!isFullScreen) {
                    TopBar(
                        onPopup = onPopup,
                        title = "Выложить видео",
                        inProgress = state.isLoading,
                        onSubmit = {
                            val input = UploadVideoInput(
                                channelId = channelId,
                                title = title,
                                source = sourceUri,
                                cover = coverUri,
                            )
                            onSubmit(input)
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            modifier = modifier
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
                        if (!it) {
                            sourceUri = null
                        }
                    }

                    val sourcePicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetContent()
                    ) {
                        if (it != null) {
                            sourceUri = it
                        }
                    }
                    val sourceErrMsg =
                        if (compoundError.equivalentTo(UploadVideoError.SOURCE_EMPTY)) {
                            "Выберите видео"
                        } else if (compoundError.equivalentTo(UploadVideoError.SOURCE_TYPE_INVALID)) {
                            "Некорректный формат видео"
                        } else null

                    val sourceActionItems = if (sourceUri == null) listOf()
                    else listOf(
                        DeletingItem(
                            deleting = {
                                sourceUri = null
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
                                placeholder = if (sourceUri == null) "Выбрать запись" else "Изменить запись",
                                onClick = {
                                    isVideoDialogOpen = true
                                },
                                icon = Icons.Rounded.VideoLibrary,
                                errorMsg = sourceErrMsg
                            )
                        }
                        val cacheDir = context.cacheDir.absolutePath
                        val cameraPermission =
                            rememberPermissionState(Manifest.permission.CAMERA)
                        ContextMenu(
                            isExpanded = isVideoDialogOpen,
                            onDismiss = {
                                isVideoDialogOpen = false
                            },
                            menuItems = listOf(
                                MenuItem(
                                    title = "Создать видео",
                                    onClick = {
                                        val isCameraPermissionGranted =
                                            cameraPermission.status.isGranted
                                        if (isCameraPermissionGranted) {
                                            val file = File(
                                                cacheDir,
                                                "tmp_file_${System.currentTimeMillis()}.mp4"
                                            )
                                            sourceUri = FileProvider.getUriForFile(
                                                context,
                                                "mikhail.shell.video.hosting.fileprovider",
                                                file
                                            )
                                            sourceCreator.launch(sourceUri!!)
                                        } else {
                                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                                    context as Activity,
                                                    "android.permission.CAMERA"
                                                )
                                            ) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Разрешите доступ к камере в настройках Вашего устройства.",
                                                        duration = SnackbarDuration.Long
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
                                    title = "Выбрать видео",
                                    onClick = { sourcePicker.launch("video/*") }
                                )
                            )
                        )
                    }
                }
                LaunchedEffect(sourceUri) {
                    if (sourceUri != null) {
                        player.setMediaItem(MediaItem.fromUri(sourceUri!!))
                        player.prepare()
                    } else {
                        player.clearMediaItems()
                    }
                }
                if (sourceUri != null) {
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
                                        Modifier.aspectRatio(if (aspectRatio < 1f) 16f / 9 else aspectRatio)
                                    }
                                ),
                            player = player,
                            onRatioObtained = {
                                aspectRatio = it
                            },
                            isFullScreen = isFullScreen,
                            onFullscreen = {
                                isFullScreen = it
                                onFullScreen(isFullScreen)
                            }
                        )
                    }
                    LaunchedEffect(isFullScreen) {
                        onFullScreen(isFullScreen)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val window = activity?.window!!
                            WindowCompat.setDecorFitsSystemWindows(window, !isFullScreen)
                            if (isFullScreen) {
                                window.insetsController?.let {
                                    it.hide(WindowInsetsCompat.Type.systemBars())
                                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                                }
                            } else {
                                window.insetsController?.show(WindowInsetsCompat.Type.systemBars())
                            }
                        }
                    }
                }
                if (!isFullScreen) {
                    val titleErrMsg =
                        if (compoundError.equivalentTo(UploadVideoError.TITLE_EMPTY)) {
                            "Заполните название"
                        } else null
                    val titleActionItems = if (title.isBlank()) emptyList() else listOf(
                        ActionItem(
                            icon = Icons.Rounded.Delete,
                            action = {
                                title = ""
                            }
                        )
                    )
                    EditField(
                        actionItems = titleActionItems
                    ) {
                        InputField(
                            modifier = Modifier.fillMaxWidth(),
                            value = title,
                            onValueChange = {
                                title = it
                            },
                            errorMsg = titleErrMsg,
                            placeholder = "Название",
                            icon = Icons.Rounded.Title
                        )
                    }

                    val channelErrMsg =
                        if (compoundError.equivalentTo(UploadVideoError.CHANNEL_NOT_CHOSEN)) {
                            "Выберите канал"
                        } else null
                    val channelActionItems = if (channelId == null) emptyList() else listOf(
                        ActionItem(
                            icon = Icons.Rounded.Delete,
                            action = {
                                channelId = null
                            }
                        )
                    )
                    EditField(
                        actionItems = channelActionItems
                    ) {
                        Dropdown(
                            selected = channelId,
                            modifier = Modifier.fillMaxWidth(),
                            placeHolder = "Канал",
                            values = state.channels.associate { it.channelId!! to it.title },
                            onValueChange = {
                                channelId = it
                            },
                            errorMsg = channelErrMsg,
                            icon = Icons.Rounded.Apps
                        )
                    }


                    val coverPicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetContent()
                    ) {
                        if (it != null)
                            coverUri = it
                    }
                    EditField(
                        actionItems = if (coverUri == null) emptyList() else listOf(
                            ActionItem(
                                icon = Icons.Rounded.Delete,
                                action = {
                                    coverUri = null
                                }
                            )
                        )
                    ) {
                        FileInputField(
                            placeholder = if (coverUri == null) "Выбрать обложку" else "Изменить обложку",
                            onClick = {
                                coverPicker.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Image
                        )
                    }
                    if (coverUri != null) {
                        Image(
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(16f / 9),
                            contentScale = ContentScale.Crop,
                            painter = rememberAsyncImagePainter(coverUri),
                            contentDescription = "Обложка для видео"
                        )
                    }
                }
            }
        }
        LaunchedEffect(state.video) {
            if (state.video != null) {
                snackbarHostState.showSnackbar(
                    message = "Вы можете отслеживать прогресс загрузки в уведомлениях.",
                    duration = SnackbarDuration.Long
                )
                onSuccess(state.video)
            }
        }
    } else if (state.error.equivalentTo(ChannelLoadingError.UNEXPECTED)) {
        ErrorComponent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            onRetry = {
                onRefresh()
            }
        )
    } else if (state.isLoading) {
        LoadingComponent(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
@Preview(
    name = "Day Theme Upload Video Screen",
    showBackground = true
)
fun UploadVideoScreenPreview() {
    val player = ExoPlayer.Builder(LocalContext.current)
        .build()
    UploadVideoScreen(
        state = UploadVideoScreenState(
            channels = listOf(
                Channel(
                    channelId = 100500,
                    ownerId = 100,
                    title = "Канал №1"
                )
            ),
        ),
        player = player,
        onSubmit = {},
        onRefresh = {},
        onSuccess = {},
        onPopup = {},
        onFullScreen = {},
    )

}

