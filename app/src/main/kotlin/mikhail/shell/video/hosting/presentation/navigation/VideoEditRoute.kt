package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditScreen
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditViewModel

fun NavGraphBuilder.videoEditRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.EditVideo> {
        val input = it.toRoute<Route.EditVideo>()
        val viewModel = hiltViewModel<VideoEditViewModel, VideoEditViewModel.Factory> {
            it.create(input.videoId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VideoEditScreen(
            state = state,
            onRefresh = {
                viewModel.loadInitialVideo()
            },
            onSubmit = { video, editAction, cover ->
                viewModel.edit(
                    video = video,
                    coverAction = editAction,
                    cover = cover
                )
            },
            onSuccess = {
                navController.navigate(Route.Video(it.videoId!!))
            },
            onCancel = {
                navController.navigate(Route.Video(it))
            }
        )
    }
}