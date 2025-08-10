package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditScreen
import mikhail.shell.video.hosting.presentation.video.edit.VideoEditViewModel
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.videoEditRoute(
    navController: NavController,
) {
    composable<Route.Video.Edit> {
        val input = it.toRoute<Route.Video.Edit>()
        val viewModel = hiltViewModel<VideoEditViewModel, VideoEditViewModel.Factory> { it.create(input.videoId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        VideoEditScreen(
            state = state,
            onRefresh = viewModel::loadInitialVideo,
            onSubmit = viewModel::edit,
            onSuccess = {
                navController.navigate(Route.Video.View(it.videoId!!))
            },
            onCancel = {
                navController.navigate(Route.Video.View(it))
            },
            onVideoNotFound = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.popBackStack()
                }
            },
            onAuthenticationRequired = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.navigate(Route.Authentication)
                }
            }
        )
    }
}