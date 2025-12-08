package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.video.search.SearchScreen
import mikhail.shell.video.hosting.presentation.video.search.SearchScreenEvent
import mikhail.shell.video.hosting.presentation.video.search.SearchViewModel

fun EntryProviderScope<Route>.searchRoute(
    rootBackStack: MutableList<Route>
) {
    entry <Route.Search.View> {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val viewModel = hiltViewModel<SearchViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        SearchScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is SearchScreenEvent.VideoChosen -> rootBackStack.add(Route.Video(event.videoId))
                is SearchScreenEvent.Failure -> {
                    if (event.error == NetworkError.AUTHENTICATION) {
                        rootBackStack.add(Route.Authentication)
                    } else {
                        coroutineScope.launch {
                            context.getStandardErrorMessage(event.error)?.let {
                                snackBarHostState.showSnackbar(it)
                            }
                        }
                    }
                }
            }
        }
    }
}