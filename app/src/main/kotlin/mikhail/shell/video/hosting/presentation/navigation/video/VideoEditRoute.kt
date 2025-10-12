package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreen
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreenState
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingUiEvent
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingViewModel

fun EntryProviderScope<Route>.editVideoRoute(
    rootBackStack: MutableList<Route>,
    videoBackStack: MutableList<Route>
) {
    entry<Route.Video.Edit> { bundle ->
        val viewModel = hiltViewModel<VideoEditingViewModel, VideoEditingViewModel.Factory> { it.create(bundle.videoId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VideoEditingScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    VideoEditingUiEvent.Cancel -> videoBackStack.removeLastOrNull()
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is VideoEditingScreenState.Failure) {
                if ((state as VideoEditingScreenState.Failure).error == NetworkError.NOT_FOUND) {
                    videoBackStack.clear()
                } else if ((state as VideoEditingScreenState.Failure).error == NetworkError.AUTHENTICATION) {
                    rootBackStack.add(Route.Authentication)
                }
            }
        }
    }
}