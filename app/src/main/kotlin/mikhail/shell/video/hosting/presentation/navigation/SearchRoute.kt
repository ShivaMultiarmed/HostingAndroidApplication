package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosScreen
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosViewModel

fun NavGraphBuilder.searchRoute() {
    composable<Route.Search> {
        val viewModel = hiltViewModel<SearchVideosViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SearchVideosScreen(
            state = state,
            onSubmit = {
                viewModel.search(it)
            },
            onScrollToBottom = { partNumber, partSize ->
                viewModel.loadVideoPart(partSize, partNumber)
            }
        )
    }
}