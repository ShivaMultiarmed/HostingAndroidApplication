package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.player.LocalPlayerState
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreen
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingViewModel
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.editVideoRoute(
    rootBackStack: MutableList<Route>,
    videoBackStack: MutableList<Route>
) {
    entry<Route.Video.Edit> { route ->
        val context = LocalContext.current
        val viewModel =
            hiltViewModel<VideoEditingViewModel, VideoEditingViewModel.Factory> { factory ->
                factory.create(route.videoId)
            }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        VideoEditingScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is ScreenEvent.Failure -> {
                    if (event.error == NetworkError.AUTHENTICATION) {
                        rootBackStack.add(Route.Authentication)
                    } else {
                        context.getStandardErrorMessage(event.error)?.let {
                            snackBarHostState.showSnackbar(it)
                        }
                    }
                }
                ScreenEvent.Cancelled -> videoBackStack.removeLastOrNull()
                ScreenEvent.Success -> {
                    val videoRoute = videoBackStack.find { it is Route.Video.View } as Route.Video.View
                    videoBackStack.clear()
                    videoBackStack.add(videoRoute.copy())
                }
            }
        }
        var playerState by LocalPlayerState.current
        RetainedEffect (Unit) {
            playerState = playerState.copy(hidden = false)
            onRetire {
                playerState = playerState.copy(hidden = true)
            }
        }
    }
}

operator fun <T> Collection<T>.get(index: Int): T {
    return if (index >= 0) elementAt(index) else elementAt(size + index)
}