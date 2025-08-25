package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreen
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingScreenState
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingUiEvent
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditingViewModel

fun NavGraphBuilder.editVideoRoute(navController: NavController) {
    composable<Route.Video.Edit> {
        val input = it.toRoute<Route.Video.Edit>()
        val viewModel = hiltViewModel<VideoEditingViewModel, VideoEditingViewModel.Factory> { it.create(input.videoId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VideoEditingScreen(
            state = state,
            onEvent = {
                when (it) {
                    VideoEditingUiEvent.Cancel -> navController.popBackStack()
                    else -> null
                }
            }
        )
        LaunchedEffect(state) {
            if (state is VideoEditingScreenState.Failure) {
                if ((state as VideoEditingScreenState.Failure).error == NetworkError.NOT_FOUND) {
                    navController.popBackStack()
                } else if ((state as VideoEditingScreenState.Failure).error == NetworkError.AUTHENTICATION) {
                    navController.navigate(Route.Authentication)
                }
            }
        }
    }
}