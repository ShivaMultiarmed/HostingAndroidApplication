package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.observeAsEvents
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingEvent
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreen
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingViewModel

fun EntryProviderScope<Route>.editVideoRoute(
    rootBackStack: MutableList<Route>,
    videoBackStack: MutableList<Route>
) {
    entry<Route.Video.Edit> { route ->
        val context = LocalContext.current
        val viewModel = hiltViewModel<VideoEditingViewModel, VideoEditingViewModel.Factory> { it.create(route.videoId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        VideoEditingScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observeAsEvents { event ->
            when (event) {
                is VideoEditingEvent.Failure if (event.error is NetworkError) -> coroutineScope.launch {
                    val errorMessage = context.getNetworkErrorMessage(event.error)
                    snackBarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                VideoEditingEvent.NavigateBack -> videoBackStack.removeLastOrNull()
                VideoEditingEvent.RequireAuthentication -> rootBackStack.add(Route.Authentication)
                VideoEditingEvent.Success -> {
                    val videoRoute = videoBackStack.find { it is Route.Video } as Route.Video
                    videoBackStack.clear()
                    videoBackStack.add(videoRoute.copy())
                }
                else -> Unit
            }
        }
    }
}