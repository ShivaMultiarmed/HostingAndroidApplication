package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.search.SearchScreen
import mikhail.shell.video.hosting.presentation.video.search.SearchScreenUiEvent
import mikhail.shell.video.hosting.presentation.video.search.SearchViewModel

fun EntryProviderScope<Route>.searchRoute(
    rootBackStack: MutableList<Route>
) {
    entry <Route.Search.View> {
        val viewModel = hiltViewModel<SearchViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SearchScreen(
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