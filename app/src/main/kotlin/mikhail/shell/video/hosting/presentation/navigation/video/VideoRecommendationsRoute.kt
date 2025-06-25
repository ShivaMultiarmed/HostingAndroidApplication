package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.recommendations.VideoRecommendationsScreen
import mikhail.shell.video.hosting.presentation.video.recommendations.VideoRecommendationsViewModel

fun NavGraphBuilder.videoRecommendationsRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Video.Recommendations> {
        val viewModel = hiltViewModel<VideoRecommendationsViewModel, VideoRecommendationsViewModel.Factory> {
            it.create(userDetailsProvider.getUserId())
        }
        val state by viewModel.stateFlow.collectAsStateWithLifecycle()
        VideoRecommendationsScreen(
            state = state,
            onLoadVideosPart = viewModel::loadNextVideosPart,
            onVideoClick = {
                navController.navigate(Route.Video.View(it))
            }
        )
    }
}