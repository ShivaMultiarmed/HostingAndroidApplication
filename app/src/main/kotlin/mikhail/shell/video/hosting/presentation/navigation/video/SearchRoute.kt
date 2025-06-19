package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosScreen
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosViewModel

fun NavGraphBuilder.searchRoute(navController: NavController) {
    composable<Route.Video.Search> {
        val viewModel = hiltViewModel<SearchVideosViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SearchVideosScreen(
            state = state,
            onSubmit = viewModel::search,
            onScrollToBottom = {
                if (!state.areAllVideosLoaded) {
                    viewModel.loadVideoPart()
                }
            },
            onVideoClick = {
                navController.navigate(Route.Video.View(it))
            }
        )
    }
}