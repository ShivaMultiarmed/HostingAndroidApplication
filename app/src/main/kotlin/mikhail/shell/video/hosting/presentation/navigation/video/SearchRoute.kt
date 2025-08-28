package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.search.SearchScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosScreen
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosViewModel

fun EntryProviderBuilder<Route>.searchRoute(
    rootBackStack: MutableList<Route>
) {
    entry <Route.Search.View> {
        val viewModel = hiltViewModel<SearchVideosViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SearchVideosScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    is SearchScreenUiEvent.ClickedVideo -> rootBackStack.add(Route.Video(event.videoId))
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