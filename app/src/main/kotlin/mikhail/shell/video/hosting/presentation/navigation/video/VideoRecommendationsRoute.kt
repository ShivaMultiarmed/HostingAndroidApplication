package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsViewModel
import mikhail.shell.video.hosting.presentation.video.recommendations.VideoRecommendationsScreen

fun EntryProviderBuilder<Route>.videoRecommendationsRoute(
    rootBackStack: MutableList<Route>,
    recommendationsBackStack: MutableList<Route>
) {
    entry <Route.Recommendations.View> {
        val viewModel = hiltViewModel<RecommendationsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        VideoRecommendationsScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    is RecommendationsScreenUiEvent.ClickedVideo -> rootBackStack.add(Route.Video.View(event.videoId))
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state.error == NetworkError.AUTHENTICATION) {
                rootBackStack.add(Route.Authentication)
            }
        }
    }
}