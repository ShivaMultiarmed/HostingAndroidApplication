package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsScreen
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsViewModel
import mikhail.shell.video.hosting.presentation.video.recommendations.RecommendationsScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.recommendationsRoute(
    rootBackStack: MutableList<Route>,
    recommendationsBackStack: MutableList<Route>
) {
    entry <Route.Recommendations.View> {
        val context = LocalContext.current
        val viewModel = hiltViewModel<RecommendationsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val snackBarHostState = remember { SnackbarHostState() }
        val events = viewModel.events
        RecommendationsScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is ScreenEvent.Failure -> {
                    if (event.error != NetworkError.AUTHENTICATION) {
                        context.getStandardErrorMessage(event.error)?.let {
                            snackBarHostState.showSnackbar(it)
                        }
                    } else {
                        rootBackStack.add(Route.Authentication)
                    }
                }
                is ScreenEvent.VideoChosen -> rootBackStack.add(Route.Video(event.videoId))
                is ScreenEvent.ChannelChosen -> recommendationsBackStack.add(Route.Channel(event.channelId))
            }
        }
    }
}