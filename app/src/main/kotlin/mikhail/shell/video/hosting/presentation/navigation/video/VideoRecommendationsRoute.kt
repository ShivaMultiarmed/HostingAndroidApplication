package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsViewModel
import mikhail.shell.video.hosting.presentation.video.recommendations.VideoRecommendationsScreen

fun NavGraphBuilder.videoRecommendationsRoute(
    navController: NavController
) {
    composable<Route.Video.Recommendations> {
        val viewModel = hiltViewModel<RecommendationsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        VideoRecommendationsScreen(
            state = state,
            onEvent = {
                when (it) {
                    is RecommendationsScreenUiEvent.ClickedVideo -> navController.navigate(Route.Video.View(it.videoId))
                    else -> null
                }
            }
        )
        LaunchedEffect(state) {
            if (state.error == NetworkError.AUTHENTICATION) {
                navController.navigate(Route.Authentication)
            }
        }
    }
}