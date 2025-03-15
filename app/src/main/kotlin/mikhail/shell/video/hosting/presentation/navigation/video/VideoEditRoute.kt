package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditScreen
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditViewModel

fun NavGraphBuilder.videoEditRoute(
    navController: NavController,
) {
    composable<Route.Video.Edit> {
        val input = it.toRoute<Route.Video.Edit>()
        val viewModel = hiltViewModel<VideoEditViewModel, VideoEditViewModel.Factory> {
            it.create(input.videoId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        VideoEditScreen(
            state = state,
            onRefresh = viewModel::loadInitialVideo,
            onSubmit = viewModel::edit,
            onSuccess = {
                navController.navigate(Route.Video.View(it.videoId!!))
            },
            onCancel = {
                navController.navigate(Route.Video.View(it))
            }
        )
    }
}