package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.recommendations.VideoRecommendationsScreen
import mikhail.shell.video.hosting.presentation.video.recommendations.VideoRecommendationsViewModel

fun NavGraphBuilder.videoRecommendationsRoute(
    navController: NavController
) {
    composable<Route.Video.Recommendations> {
        val viewModel = hiltViewModel<VideoRecommendationsViewModel>()
        val state by viewModel.stateFlow.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        VideoRecommendationsScreen(
            state = state,
            onLoadVideosPart = viewModel::loadNextVideosPart,
            onVideoClick = {
                navController.navigate(Route.Video.View(it))
            },
            onAuthenticationRequired = {
                coroutineScope.launch {
                    delay(800)
                    navController.navigate(Route.Authentication)
                }
            }
        )
    }
}